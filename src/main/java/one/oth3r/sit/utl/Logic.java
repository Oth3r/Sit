package one.oth3r.sit.utl;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import one.oth3r.sit.file.*;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class Logic {

    /**
     * checks if the player can sit at the block specified
     * @param player the player that's going to sit
     * @param blockPos the position that the player is going to sit at
     * @param hitResult nullable, not null if the player is sitting with their hand
     * @return true if the player can sit with the conditions provided
     */
    public static boolean canSit(ServerPlayerEntity player, BlockPos blockPos, @Nullable BlockHitResult hitResult) {
        // cant sit if crouching
        if (player.isSneaking()) return false;

        // if sitting on a sit entity and sit while seated off, false
        if (!FileData.getServerConfig().canSitWhileSeated() && Data.getSitEntity(player) != null) return false;

        // if hit result isnt null (check the hands of the player) & the player hand checker returns false (can't sit with the items in the hand), quit
        if (hitResult != null) {
            if (!checkHands(player)) return false;
        }

        // check if the block is in the right y level limits from the config
        if (!checkYLimits(player, blockPos)) return false;

        ServerWorld serverWorld = player.getWorld();

        Double sitHeight = Utl.getSittingHeight(player,blockPos,hitResult);

        // if the sit height is null, it's not a sittable block
        if (sitHeight == null) return false;

        DisplayEntity.TextDisplayEntity entity = Utl.Entity.create(serverWorld,blockPos,sitHeight);

        // checks if the player can sit
        return checkPlayerSitAbility(entity);
    }

    /**
     * makes the player attempt to sit at the position provided (checks if the player can sit before)
     * @param player the player that is sitting
     * @param blockPos the pos the player is going to sit at
     * @param hitResult nullable, not null if the player is sitting with their hand
     * @return true if sitting was successful
     */
    public static boolean sit(ServerPlayerEntity player, BlockPos blockPos, @Nullable BlockHitResult hitResult) {
        if (!canSit(player, blockPos, hitResult)) return false;
        // assets
        ServerWorld serverWorld = player.getWorld();
        Double sitHeight = Utl.getSittingHeight(player,blockPos,hitResult);
        // shouldn't be null because we already checked, but do another check to clear IDE errors
        assert sitHeight != null;

        // spawn the entity and make the player sit
        Utl.Entity.spawnSit(player, Utl.Entity.create(serverWorld,blockPos,sitHeight));

        return true;
    }

    /**
     * makes the player attempt to sit at the block they are looking at (range of 5)
     * @param player the player who is trying to sit
     * @return true if sitting was successful
     */
    public static boolean sitLooking(ServerPlayerEntity player) {
        return sit(player, Utl.getBlockPosPlayerIsLookingAt(player.getWorld(),player,
                Utl.getPlayerReach(player)),null);
    }

    /**
     * checks the hands of the player and the items in each hand and sees if the player can sit down
     */
    public static boolean checkHands(ServerPlayerEntity player) {
        SittingConfig sittingConfig = FileData.getPlayerSetting(player);
        // if can't sit with hand, false
        if (!sittingConfig.canSitWithHand()) return false;

        // a boolean that shows if the player can sit or not
        boolean canSit = true;

        // for each hand
        for (Hand hand : Hand.values()) {
            // if they can't sit, no need to run extra code
            if (!canSit) break;

            HandSetting handSetting = sittingConfig.getHand(hand);
            switch (handSetting.getSittingRequirement()) {
                case EMPTY -> canSit = player.getStackInHand(hand).isEmpty();
                case FILTER -> canSit = Utl.checkItem(handSetting.getFilter(), player.getStackInHand(hand));
            }
        }
        // return the output of the check
        return canSit;
    }

    /**
     * check if the Y-level of the block is within the limits of the player, bounds are set in the {@link ServerConfig}
     */
    public static boolean checkYLimits(ServerPlayerEntity player, BlockPos blockPos) {
        double playerY = player.getBlockY();
        double blockY = blockPos.getY();
        // if the block is above the eye height
        boolean isAbove = playerY < blockY;

        // return true if equal
        if (playerY == blockY) return true;

        // get the height difference (positive)
        double heightDifference = Math.abs(playerY - blockY);
        // get the config limits
        ServerConfig.YDifferenceLimit yDifferenceLimit = FileData.getServerConfig().getYDifferenceLimit();

        return (isAbove? yDifferenceLimit.getAbove() : yDifferenceLimit.getBelow()) >= heightDifference;
    }

    /**
     * removes the entity bound to the player from the game, using the player
     */
    public static void removeEntity(ServerPlayerEntity player) {
        DisplayEntity.TextDisplayEntity entity = Data.getSitEntity(player);
        // make sure the player has a sit entity bounded to them
        if (entity == null) return;

        // remove the entity
        Utl.Entity.remove(entity);
    }

    /**
     * spawns a sit entity for the player, they HAVE TO BE in the spawn list
     */
    public static void spawnEntity(ServerPlayerEntity player) {
        // return if not in the list
        if (Data.getSpawnList().get(player) == null) return;

        // if the player is already sitting on a sit entity, remove it before spawning a new one
        if (Data.getSitEntity(player) != null) Logic.removeEntity(player);
        // get the new entity
        DisplayEntity.TextDisplayEntity sitEntity = Data.getSpawnList().get(player);
        // spawn and ride the entity
        player.getWorld().spawnEntity(sitEntity);
        player.startRiding(sitEntity);
        // add the entity to the list
        Data.addSitEntity(player, sitEntity);
        // remove the entity from the spawn list
        Data.removeSpawnList(player);
    }

    /**
     * checks if the player should still be sitting, e.g. the block was destroyed ect.
     */
    public static void checkSittingValidity(ServerPlayerEntity player) {
        DisplayEntity.TextDisplayEntity entity = Data.getSitEntity(player);
        // make sure the player has a sit entity bounded to them
        if (entity == null) return;

        // if the entity location isn't valid anymore, remove it
        if (!Utl.Entity.isValid(player,entity)) {
            removeEntity(player);
        }
    }

    /**
     * checks if entity would cause the player to suffocate when sitting
     * @param entity the entity
     * @return true if there is no obstruction
     */
    public static boolean checkPlayerSitAbility(DisplayEntity.TextDisplayEntity entity) {
        // get the entity's block pos
        BlockPos pos = Utl.Entity.getBlockPos(entity);
        // get the poses to check above the block
        BlockPos pos1 = new BlockPos(pos).add(0,1,0), pos2 = new BlockPos(pos).add(0,2,0), posBelow = new BlockPos(pos);
        // doesn't check 2 blocks above if not sitting above .80 of the block
        if (pos.getY() > (entity.getY()-Utl.Entity.Y_ADJUSTMENT) - .80) {
            pos2 = pos2.add(0,-1,0);
            posBelow = posBelow.add(0,-1,0);
        }

        // check if both poses are obstructed or not
        return Utl.isNotObstructed(entity.getWorld(),pos1) && Utl.isNotObstructed(entity.getWorld(),pos2)
                // also check if occupied, checking below to make sure you cant sit directly on top of another sit entity
                && Utl.isNotOccupied(pos) && Utl.isNotOccupied(pos1) && Utl.isNotOccupied(pos2) && Utl.isNotOccupied(posBelow);
    }

    /**
     * reloads the config files
     */
    public static void reload() {
        FileData.loadFiles();
        FileData.saveFiles();
    }

    /**
     * toggles the sit ablity config option
     * @return returns a message, that can be sent to the player
     */
    public static MutableText toggleSiting() {
        if (Data.isSupportedServer()) {
            // get the sitting config
            SittingConfig config = FileData.getSittingConfig();
            // toggle the setting
            config.setEnabled(!config.getEnabled());

            // set the sitting config to the new value
            FileData.setSittingConfig(config);
            // save the changes to the file
            config.save();
            // send the changes to the server
            Utl.sendSettingsPackets();


            // get the message settings
            String messageKey = "sit!.chat.toggle_sit."+(config.getEnabled()?"on":"off");
            Formatting messageColor = config.getEnabled()?Formatting.GREEN:Formatting.RED;

            // send the player the actionbar message
            return Chat.lang("sit!.chat.toggle_sit",
                    Chat.lang(messageKey).color(config.getEnabled()? Color.GREEN : Color.RED)).b();
        } else {
            // unsupported server message if not in a Sit! server
            return Chat.lang("sit!.chat.unsupported")
                    .color(Color.RED).b();
        }
    }

}
