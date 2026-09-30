package one.oth3r.sit.utl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;

public class LoopManager {

    private static int time = 0;

    public static void tick() {
        time++;
        if (time >= 5) {
            time = 0;

            // check all sit entities to make sure their still valid
            HashMap<ServerPlayer, Display.TextDisplay> entities = Data.getSitEntities();
            for (ServerPlayer player : entities.keySet()) {
                Display.TextDisplay entity = entities.get(player);

                if (player.getVehicle() == null || !player.getVehicle().equals(entity)) {
                    Logic.removeEntity(player);
                } else {
                    Logic.checkSittingValidity(player);
                }
            }

            // get the player's sit entity when they join
            HashMap<ServerPlayer, Integer> checkPlayers = Data.getCheckPlayers();
            for (ServerPlayer player : checkPlayers.keySet()) {
                Integer time = checkPlayers.get(player);
                // tick down or remove the player if at the end
                time -= 1;
                if (time <= 0) Data.removeCheckPlayer(player);
                else Data.setCheckPlayer(player, time);

                if (player.getVehicle() != null) {
                    Entity entity = player.getVehicle();
                    if (entity instanceof Display.TextDisplay tde && entity.getName().getString().equals(Data.ENTITY_NAME)) {
                        // bind the entity to the player
                        Data.addSitEntity(player, tde);
                        // check if the player is still allowed to sit
                        Logic.checkSittingValidity(player);
                        // remove the player from the check
                        Data.removeCheckPlayer(player);
                    }
                }
            }

            // spawn entities for everyone in the spawn list
            HashMap<ServerPlayer, Display.TextDisplay> spawnList = Data.getSpawnList();
            for (ServerPlayer player : spawnList.keySet()) {
                Logic.spawnEntity(player);
            }
        }
    }
}
