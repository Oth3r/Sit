package one.oth3r.sit.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayDismountMixin extends Display {
    public TextDisplayDismountMixin(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        // get the passenger's horizontal rotation, rotated counterclockwise, because the method rotates it clockwise for some reason
        int[][] offset = DismountHelper.offsetsForDirection(passenger.getDirection().getCounterClockWise());
        // new array with another slot
        int[][] dismountOffsets = new int[offset.length + 1][];
        // add an empty offset to the start of the array
        dismountOffsets[0] = new int[]{0, 0};
        // copy the original elements into the new array starting from index 1
        System.arraycopy(offset, 0, dismountOffsets, 1, offset.length);

        BlockPos blockPos = this.blockPosition();

        for (Pose entityPose : passenger.getDismountPoses()) {
            Vec3 vec3d = getDismountPos(passenger, entityPose, dismountOffsets, blockPos);

            // check around the block above
            if (vec3d == null) vec3d = getDismountPos(passenger, entityPose, dismountOffsets, blockPos.above());

            if (vec3d != null) return vec3d;

        }

        return super.getDismountLocationForPassenger(passenger);
    }

    /**
     * searches around the BlockPos for a stable dismount spot using the dismountOffsets
     * @param passenger the passenger to check
     * @param entityPose the pose of the passenger to check
     * @param dismountOffsets the positions to check around the BlockPos
     * @param blockPos the BlockPos to check around
     * @return the Vec3d to dismount at, null if not found
     */
    @Unique
    private @Nullable Vec3 getDismountPos(LivingEntity passenger, Pose entityPose, int[][] dismountOffsets, BlockPos blockPos) {
        // iterate through all dismount offsets
        for (int[] offset : dismountOffsets) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            mutable.set(blockPos.getX() + offset[0], blockPos.getY(), blockPos.getZ() + offset[1]);

            double dismountHeight = this.level().getBlockFloorHeight(mutable);
            if (DismountHelper.isBlockFloorValid(dismountHeight)) {
                Vec3 vec3d = Vec3.upFromBottomCenterOf(mutable, dismountHeight);

                AABB boundingBox = passenger.getLocalBoundsForPose(entityPose);
                if (DismountHelper.canDismountTo(this.level(), passenger, boundingBox.move(vec3d))) {
                    passenger.setPose(entityPose);
                    return vec3d;
                }
            }
        }
        return null;
    }
}
