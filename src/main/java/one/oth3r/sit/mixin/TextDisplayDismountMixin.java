package one.oth3r.sit.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayDismountMixin extends DisplayEntity {
    public TextDisplayDismountMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        // get the passenger's horizontal rotation, rotated counterclockwise, because the method rotates it clockwise for some reason
        int[][] offset = Dismounting.getDismountOffsets(passenger.getHorizontalFacing().rotateYCounterclockwise());
        // new array with another slot
        int[][] dismountOffsets = new int[offset.length + 1][];
        // add an empty offset to the start of the array
        dismountOffsets[0] = new int[]{0, 0};
        // copy the original elements into the new array starting from index 1
        System.arraycopy(offset, 0, dismountOffsets, 1, offset.length);

        BlockPos blockPos = this.getBlockPos();

        for (EntityPose entityPose : passenger.getPoses()) {
            Vec3d vec3d = getDismountPos(passenger, entityPose, dismountOffsets, blockPos);

            // check around the block above
            if (vec3d == null) vec3d = getDismountPos(passenger, entityPose, dismountOffsets, blockPos.up());

            if (vec3d != null) return vec3d;

        }

        return super.updatePassengerForDismount(passenger);
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
    private @Nullable Vec3d getDismountPos(LivingEntity passenger, EntityPose entityPose, int[][] dismountOffsets, BlockPos blockPos) {
        // iterate through all dismount offsets
        for (int[] offset : dismountOffsets) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            mutable.set(blockPos.getX() + offset[0], blockPos.getY(), blockPos.getZ() + offset[1]);

            double dismountHeight = this.getEntityWorld().getDismountHeight(mutable);
            if (Dismounting.canDismountInBlock(dismountHeight)) {
                Vec3d vec3d = Vec3d.ofCenter(mutable, dismountHeight);

                Box boundingBox = passenger.getBoundingBox(entityPose);
                if (Dismounting.canPlaceEntityAt(this.getEntityWorld(), passenger, boundingBox.offset(vec3d))) {
                    passenger.setPose(entityPose);
                    return vec3d;
                }
            }
        }
        return null;
    }
}
