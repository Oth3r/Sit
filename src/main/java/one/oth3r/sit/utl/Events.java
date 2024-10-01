package one.oth3r.sit.utl;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import one.oth3r.sit.command.SitCommand;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.file.LangReader;
import one.oth3r.sit.file.SittingConfig;
import one.oth3r.sit.packet.SitPayloads;
import one.oth3r.sit.screen.ConfigScreen;
import org.lwjgl.glfw.GLFW;

public class Events {

    private static class Keybindings {
        private static KeyBinding toggle_key;
        private static KeyBinding sit_key;
        private static KeyBinding config__key;

        private static void register() {
            toggle_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.toggle",
                    GLFW.GLFW_KEY_UNKNOWN,
                    "category.sit"
            ));
            sit_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.sit",
                    GLFW.GLFW_KEY_UNKNOWN,
                    "category.sit"
            ));
            config__key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.config",
                    GLFW.GLFW_KEY_UNKNOWN,
                    "category.sit"
            ));
        }

        private static void loopLogic(MinecraftClient client) {
            ClientPlayerEntity player = client.player;

            while (config__key.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen));
            }

            // anything below uses the player object, make sure it's not null
            if (player == null) return;

            while (toggle_key.wasPressed()) {
                if (Data.isInGame()) {
                    player.sendMessage(Logic.toggleSiting(), true);
                }
            }

            while (sit_key.wasPressed()) {
                // just send the sit command
                if (Data.isInGame()) {
                    if (Data.isSupportedServer()) {
                        player.networkHandler.sendCommand("sit");
                    } else {
                        // unsupported server message if not in a Sit! server
                        player.sendMessage(Utl.lang("msg.unsupported")
                                .formatted(Formatting.RED), true);
                    }
                }
            }
        }
    }

    private static class Packet {
        private static void common() {
            // register the data
            PayloadTypeRegistry.playC2S().register(SitPayloads.SettingsPayload.ID, SitPayloads.SettingsPayload.CODEC);

            PayloadTypeRegistry.playS2C().register(SitPayloads.ResponsePayload.ID, SitPayloads.ResponsePayload.CODEC);

            // server receiver is common

            /// receiving the sitting setting payload
            ServerPlayNetworking.registerGlobalReceiver(SitPayloads.SettingsPayload.ID,((payload, context) -> Data.getServer().execute(() -> {
                // save the setting on the server for that player
                FileData.setPlayerSetting(context.player(),Utl.getGson().fromJson(payload.value(), SittingConfig.class));

                // send the player back a response packet for confirmation
                ServerPlayNetworking.send(context.player(),new SitPayloads.ResponsePayload(SitPayloads.ResponsePayload.VERSION));

                // log the receiving of the packet from the player
                Data.LOGGER.info(Utl.lang("sit!.console.player_settings",context.player().getName().getString()).getString());
            })));
        }

        private static void client() {
            /// receiving the response packet from the server
            ClientPlayNetworking.registerGlobalReceiver(SitPayloads.ResponsePayload.ID, ((payload, context) -> {
                // only update when needed
                if (!Data.isSupportedServer()) {
                    Data.setSupportedServer(true);
                    Data.LOGGER.info(Utl.lang("sit!.console.connected",payload.value()).getString());
                }
            }));
        }
    }

    private static void clientMisc() {
        // client tick loop
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            assert client.player != null;
            Keybindings.loopLogic(client);
        });
    }

    /**
     * registers all client connection code
     */
    private static void clientConnections() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Data.setInGame(true);
            if (client.isInSingleplayer()) Data.setSingleplayer(true);
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
            LangReader.loadLanguageFile();

            // right click on block event
            UseBlockCallback.EVENT.register((pl, world, hand, hitResult) -> {
                if (Data.isClient() && !Data.isSingleplayer()) return ActionResult.PASS;
                // get the server player
                ServerPlayerEntity player = Data.getServer().getPlayerManager().getPlayer(pl.getUuid());

                // make sure the player isn't null, and make sure they aren't in spectator
                if (player == null || player.isSpectator()) return ActionResult.PASS;

                // consume if sitting, if not pass
                return Logic.sit(player,hitResult.getBlockPos(),hitResult) ? ActionResult.CONSUME : ActionResult.PASS;
            });

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
        clientMisc();
        Packet.client();
    }
}
