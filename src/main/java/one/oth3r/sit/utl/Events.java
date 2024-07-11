package one.oth3r.sit.utl;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import one.oth3r.sit.Sit;
import one.oth3r.sit.command.SitCommand;
import one.oth3r.sit.file.Data;

public class Events {

    public static void playerConnections() {
        // PLAYER JOIN
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Data.setPlayerSetting(handler.player,Data.getHandConfig());
            Data.setCheckPlayer(handler.player, 5);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // if keep is off, remove the entity
            if (!Data.getServerConfig().isKeepActive()) {
                Logic.removeEntity(handler.player);
            }
            Data.removePlayerSetting(handler.player);
        });
    }

    public static void server() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            Sit.server = s;
            Sit.commandManager = s.getCommandManager();

            // right click on block event
            UseBlockCallback.EVENT.register((pl, world, hand, hitResult) -> {
                // get the server player
                ServerPlayerEntity player = Sit.server.getPlayerManager().getPlayer(pl.getUuid());

                // make sure the player isn't null, and make sure they aren't in spectator
                if (player == null || player.isSpectator()) return ActionResult.PASS;

                // consume if sitting, if not pass
                return Logic.sit(player,hitResult.getBlockPos(),hitResult)? ActionResult.CONSUME : ActionResult.PASS;
            });

        });

        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            // clear all player settings (singleplayer and such)
            Data.clearPlayerSettings();
        });
    }

    public static void misc() {
        // loop setup
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> minecraftServer.execute(LoopManager::tick));

        // command setup
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SitCommand.register(dispatcher));
    }

    public static void registerCommon() {
        playerConnections();
        server();
        misc();
    }
}
