package one.oth3r.sit.utl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Display;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
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
    public static boolean canSit(ServerPlayer player, BlockPos blockPos, @Nullable BlockHitResult hitResult) {
        // cant sit if crouching
        if (player.isShiftKeyDown()) return false;

        // if sitting on a sit entity and sit while seated off, false
        if (!FileData.getServerConfig().canSitWhileSeated() && Data.getSitEntity(player) != null) return false;

        // if hit result isnt null (check the hands of the player) & the player hand checker returns false (can't sit with the items in the hand), quit
        if (hitResult != null) {
            if (!checkHands(player)) return false;
        }

        // check if the block is in the right y level limits from the config
        if (!checkYLimits(player, blockPos)) return false;

        ServerLevel serverWorld = player.level();

        Double sitHeight = Utl.getSittingHeight(player,blockPos,hitResult);

        // if the sit height is null, it's not a sittable block
        if (sitHeight == null) return false;

        Display.TextDisplay entity = Utl.Entity.create(serverWorld,blockPos,sitHeight);

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
    public static boolean sit(ServerPlayer player, BlockPos blockPos, @Nullable BlockHitResult hitResult) {
        if (!canSit(player, blockPos, hitResult)) return false;
        // assets
        ServerLevel serverWorld = player.level();
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
    public static boolean sitLooking(ServerPlayer player) {
        return sit(player, Utl.getBlockPosPlayerIsLookingAt(player.level(),player,
                Utl.getPlayerReach(player)),null);
    }

    /**
     * checks the hands of the player and the items in each hand and sees if the player can sit down
     */
    public static boolean checkHands(ServerPlayer player) {
        SittingConfig sittingConfig = FileData.getPlayerSetting(player);
        // if can't sit with hand, false
        if (!sittingConfig.canSitWithHand()) return false;

        // a boolean that shows if the player can sit or not
        boolean canSit = true;

        // for each hand
        for (InteractionHand hand : InteractionHand.values()) {
            // if they can't sit, no need to run extra code
            if (!canSit) break;

            HandSetting handSetting = sittingConfig.getHand(hand);
            switch (handSetting.getSittingRequirement()) {
                case EMPTY -> canSit = player.getItemInHand(hand).isEmpty();
                case FILTER -> canSit = Utl.checkItem(handSetting.getFilter(), player.getItemInHand(hand));
            }
        }
        // return the output of the check
        return canSit;
    }

    /**
     * check if the Y-level of the block is within the limits of the player, bounds are set in the {@link ServerConfig}
     */
    public static boolean checkYLimits(ServerPlayer player, BlockPos blockPos) {
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
    public static void removeEntity(ServerPlayer player) {
        Display.TextDisplay entity = Data.getSitEntity(player);
        // make sure the player has a sit entity bounded to them
        if (entity == null) return;

        // remove the entity
        Utl.Entity.remove(entity);
    }

    /**
     * spawns a sit entity for the player, they HAVE TO BE in the spawn list
     */
    public static void spawnEntity(ServerPlayer player) {
        // return if not in the list
        if (Data.getSpawnList().get(player) == null) return;

        // if the player is already sitting on a sit entity, remove it before spawning a new one
        if (Data.getSitEntity(player) != null) Logic.removeEntity(player);
        // get the new entity
        Display.TextDisplay sitEntity = Data.getSpawnList().get(player);
        // spawn and ride the entity
        player.level().addFreshEntity(sitEntity);
        player.startRiding(sitEntity);
        // add the entity to the list
        Data.addSitEntity(player, sitEntity);
        // remove the entity from the spawn list
        Data.removeSpawnList(player);
    }

    /**
     * checks if the player should still be sitting, e.g. the block was destroyed ect.
     */
    public static void checkSittingValidity(ServerPlayer player) {
        Display.TextDisplay entity = Data.getSitEntity(player);
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
    public static boolean checkPlayerSitAbility(Display.TextDisplay entity) {
        // get the entity's block pos
        BlockPos pos = Utl.Entity.getBlockPos(entity);
        // get the poses to check above the block
        BlockPos pos1 = new BlockPos(pos).offset(0,1,0), pos2 = new BlockPos(pos).offset(0,2,0), posBelow = new BlockPos(pos);
        // doesn't check 2 blocks above if not sitting above .80 of the block
        if (pos.getY() > (entity.getY()-Utl.Entity.Y_ADJUSTMENT) - .80) {
            pos2 = pos2.offset(0,-1,0);
            posBelow = posBelow.offset(0,-1,0);
        }

        // check if both poses are obstructed or not
        return Utl.isNotObstructed(entity.level(),pos1) && Utl.isNotObstructed(entity.level(),pos2)
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
     * toggles the sit ability config option
     * @return returns a message, that can be sent to the player
     */
    public static MutableComponent toggleSiting() {
        if (Data.isSupportedServer()) {
            // get the sitting config
            SittingConfig config = FileData.getSittingConfig();
            // toggle the setting
            config.setEnabled(!config.getEnabled());
            // save the changes to the file
            config.save();
            // send the changes to the server
            Utl.sendSettingsPackets();


            // get the message settings
            String messageKey = "sit!.chat.toggle_sit."+(config.getEnabled()?"on":"off");
            ChatFormatting messageColor = config.getEnabled()?ChatFormatting.GREEN:ChatFormatting.RED;

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
