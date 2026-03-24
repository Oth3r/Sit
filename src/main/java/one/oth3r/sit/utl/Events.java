package one.oth3r.sit.utl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import one.oth3r.sit.SitClient;
import one.oth3r.sit.command.SitCommand;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.file.SittingConfig;
import one.oth3r.sit.packet.SitPayloads;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class Events {

    private static class Keybindings {
        private static KeyMapping toggle_key;
        private static KeyMapping sit_key;
        private static KeyMapping config__key;

        private static void register() {
            KeyMapping.Category sitCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Data.MOD_ID, "main"));

            toggle_key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.sit!.toggle",
                    GLFW.GLFW_KEY_UNKNOWN,
                    sitCategory
            ));
            sit_key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.sit!.sit",
                    GLFW.GLFW_KEY_UNKNOWN,
                    sitCategory
            ));
            config__key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.sit!.config",
                    GLFW.GLFW_KEY_UNKNOWN,
                    sitCategory
            ));

            // client tick loop
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                assert client.player != null;
                Keybindings.loopLogic(client);
            });
        }

        private static void loopLogic(Minecraft client) {
            LocalPlayer player = client.player;

            while (config__key.consumeClick()) {
                client.setScreen(SitClient.getConfigScreen(client.screen));
            }

            /// anything below uses the player object, make sure it's not null
            if (player == null) return;

            while (toggle_key.consumeClick()) {
                if (Data.isInGame()) {
                    player.sendOverlayMessage(Logic.toggleSiting());
                }
            }

            while (sit_key.consumeClick()) {
                // just send the sit command
                if (Data.isInGame()) {
                    if (Data.isSupportedServer()) {
                        player.connection.sendCommand("sit");
                    } else {
                        // unsupported server message if not in a Sit! server
                        player.sendOverlayMessage(Chat.lang("sit!.chat.unsupported")
                                .color(Color.RED).b());
                    }
                }
            }
        }
    }

    private static class Packet {
        private static void common() {
            // register the data
            PayloadTypeRegistry.serverboundPlay().register(SitPayloads.SettingsPayload.ID, SitPayloads.SettingsPayload.CODEC);

            PayloadTypeRegistry.clientboundPlay().register(SitPayloads.ResponsePayload.ID, SitPayloads.ResponsePayload.CODEC);

            // server receiver is common

            /// receiving the sitting setting payload
            ServerPlayNetworking.registerGlobalReceiver(SitPayloads.SettingsPayload.ID,((payload, context) -> Data.getServer().execute(() -> {
                // save the setting on the server for that player
                FileData.setPlayerSetting(context.player(),Utl.getGson().fromJson(payload.value(), SittingConfig.class));

                // send the player back a response packet for confirmation
                ServerPlayNetworking.send(context.player(),new SitPayloads.ResponsePayload(SitPayloads.ResponsePayload.VERSION));

                // log the receiving of the packet from the player
                Data.LOGGER.info(Chat.lang("sit!.console.player_settings",context.player().getName().getString()).toString());
            })));
        }

        private static void client() {
            /// receiving the response packet from the server
            ClientPlayNetworking.registerGlobalReceiver(SitPayloads.ResponsePayload.ID, ((payload, context) -> {
                // only update when needed
                if (!Data.isSupportedServer()) {
                    Data.setSupportedServer(true);
                    Data.LOGGER.info(Chat.lang("sit!.console.connected",payload.value()).toString());
                }
            }));
        }
    }

    /**
     * registers all client connection code
     */
    private static void clientConnections() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Data.setInGame(true);
            if (client.isLocalServer()) Data.setSingleplayer(true);
            // send a data packet whenever joining a server
            Utl.sendSettingsPackets();
        });

        // reset cashed things on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Data.setInGame(false);
            Data.setSingleplayer(false);
            Data.setSupportedServer(false);
        });
    }

    /**
     * registers all common server player connection code
     */
    private static void playerConnections() {
        // PLAYER JOIN
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FileData.setPlayerSetting(handler.player, FileData.getSittingConfig());
            Data.setCheckPlayer(handler.player, 5);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // if keep is off, remove the entity
            if (!FileData.getServerConfig().isKeepActive()) {
                Logic.removeEntity(handler.player);
            }
            FileData.removePlayerSetting(handler.player);
        });
    }

    /**
     * registers all server lifecycle events
     */
    private static void serverLifecycle() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            Data.setServer(s);
        });

        // right click on block event
        UseBlockCallback.EVENT.register((pl, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            // get the server player
            ServerPlayer player = Data.getServer().getPlayerList().getPlayer(pl.getUUID());

            // make sure the player isn't null, and make sure they aren't in spectator
            if (player == null || player.isSpectator()) return InteractionResult.PASS;

            // consume if sitting, if not pass
            InteractionResult result = Logic.canSit(player,hitResult.getBlockPos(),hitResult) ? InteractionResult.CONSUME : InteractionResult.PASS;
            // todo test
            if (result.equals(InteractionResult.CONSUME)) {
                try {
                    CommandDispatcher<CommandSourceStack> dispatcher = Data.getServer().createCommandSourceStack().dispatcher();
                    ParseResults<CommandSourceStack> parse = dispatcher.parse("sit", player.createCommandSourceStack());
                    dispatcher.execute(parse);
                } catch (CommandSyntaxException e) {
                    Data.LOGGER.error("Error executing sit command for player %s", player.getName().getString());
                }
            }

            return result;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            // clear the server
            Data.setServer(null);
            // clear all player settings (singleplayer and such)
            FileData.clearPlayerSettings();
        });

        // server loop setup
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> minecraftServer.execute(LoopManager::tick));

        // server command setup
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SitCommand.register(dispatcher));
    }

    // a one call method for the common and client

    public static void registerCommon() {
        playerConnections();
        serverLifecycle();
        Packet.common();
    }

    public static void registerClient() {
        Keybindings.register();
        clientConnections();
        Packet.client();
    }
}
