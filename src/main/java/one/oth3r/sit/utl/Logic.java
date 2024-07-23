package one.oth3r.sit.utl;

import net.minecraft.block.*;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.file.SittingConfig;
import one.oth3r.sit.file.HandSetting;
import org.jetbrains.annotations.Nullable;

public class Logic {
    public static boolean sit(ServerPlayerEntity player, BlockPos blockPos, @Nullable BlockHitResult hitResult) {
        // cant sit if crouching
        if (player.isSneaking()) return false;

        // if sitting on a stair and sit while seated off, false
        if (FileData.getSitEntity(player) != null && !FileData.getServerConfig().isSitWhileSeated()) return false;

        // if hit result isnt null (check the hands of the player) & the player hand checker returns false (can't sit with the items in the hand), quit
        if (hitResult != null) {
            if (!checkHands(player)) return false;
        }

        ServerWorld serverWorld = player.getServerWorld();
        BlockState blockState = serverWorld.getBlockState(blockPos);

        Double sitHeight = Utl.getSittingHeight(blockState,player,blockPos,hitResult);

        // if the sit height is null, its not a sittable block
        if (sitHeight == null) return false;

        DisplayEntity.TextDisplayEntity entity = Utl.Entity.create(serverWorld,blockPos,sitHeight);

        if (!checkPlayerSitAbility(entity)) return false;

        Utl.Entity.spawnSit(player, entity);

        return true;
    }

    /**
     * checks the hands of the player and the items in each hand and sees if the player can sit down
     */
    public static boolean checkHands(ServerPlayerEntity player) {
        SittingConfig sittingConfig = FileData.getPlayerSetting(player);
        // if can't sit with hand, false
        if (!sittingConfig.canSitWithHand()) return false;

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
     * removes the entity from the game, using the player
     */
    public static void removeEntity(ServerPlayerEntity player) {
        DisplayEntity.TextDisplayEntity entity = FileData.getSitEntity(player);
        // make sure the player has a sit entity bounded to them
        if (entity == null) return;

        // remove the entity
        Utl.Entity.remove(entity);
    }

    /**
     * checks if the player should still be sitting, e.g. the block was destroyed ect.
     */
    public static void checkSittingValidity(ServerPlayerEntity player) {
        DisplayEntity.TextDisplayEntity entity = FileData.getSitEntity(player);
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
        BlockPos pos1 = new BlockPos(pos).add(0,1,0), pos2 = new BlockPos(pos).add(0,2,0);
        // doesn't check 2 blocks above if not sitting above .80 of the block
        if (pos.getY() > entity.getY() - .80) pos2 = pos2.add(0,-1,0);

        // check if both poses are obstructed or not
        return Utl.isNotObstructed(entity.getWorld(),pos1) && Utl.isNotObstructed(entity.getWorld(),pos2);
    }

    /**
     * reloads the config files
     */
    public static void reload() {
        FileData.loadFiles(false);
    }

}
