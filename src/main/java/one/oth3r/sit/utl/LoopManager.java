package one.oth3r.sit.utl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import one.oth3r.sit.file.FileData;

import java.util.HashMap;

public class LoopManager {

    private static int time = 0;

    public static void tick() {
        time++;
        if (time >= 5) {
            time = 0;

            // check all sit entities to make sure their still valid
            HashMap<ServerPlayerEntity, DisplayEntity.TextDisplayEntity> entities = FileData.getSitEntities();
            for (ServerPlayerEntity player : entities.keySet()) {
                DisplayEntity.TextDisplayEntity entity = entities.get(player);

                if (player.getVehicle() == null || !player.getVehicle().equals(entity)) {
                    Logic.removeEntity(player);
                } else {
                    Logic.checkSittingValidity(player);
                }
            }

            // get the player's sit entity when they join
            // todo make it so it updates the sitting height and pos based on the block so if it changed while offline it still works (or if stair changes shape)
            HashMap<ServerPlayerEntity, Integer> checkPlayers = FileData.getCheckPlayers();
            for (ServerPlayerEntity player : checkPlayers.keySet()) {
                Integer time = checkPlayers.get(player);
                // tick down or remove the player if at the end
                time -= 1;
                if (time <= 0) FileData.removeCheckPlayer(player);
                else FileData.setCheckPlayer(player, time);

                if (player.getVehicle() != null) {
                    Entity entity = player.getVehicle();
                    if (entity instanceof DisplayEntity.TextDisplayEntity tde && entity.getName().getString().equals(Data.ENTITY_NAME)) {
                        // bind the entity to the player
                        FileData.addSitEntity(player, tde);
                        // check if the player is still allowed to sit
                        Logic.checkSittingValidity(player);
                        // remove the player from the check
                        FileData.removeCheckPlayer(player);
                    }
                }
            }
        }
    }
}
