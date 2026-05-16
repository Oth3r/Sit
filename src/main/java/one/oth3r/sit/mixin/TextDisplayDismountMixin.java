package one.oth3r.sit.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayDismountMixin extends Display {
    @Unique
    private static final float sit$BODY_ROTATION_MIN_STEP = 0.1F;
    @Unique
    private static final float sit$BODY_ROTATION_DEADZONE = 45.0F;
    @Unique
    private static final float sit$BODY_ROTATION_MAX_STEP = 18.0F;
    @Unique
    private static final float sit$BODY_ROTATION_FULL_SPEED_AT = 120.0F;

    public TextDisplayDismountMixin(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public @NonNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
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

    @Override
    public void positionRider(@NonNull Entity passenger, @NonNull MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        this.sit$syncPassengerRotation(passenger);
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        float targetYaw = passenger.getYRot();
        float yaw = this.getYRot();
        float yawDifference = Math.abs(Mth.wrapDegrees(targetYaw - yaw));

        if (yawDifference > sit$BODY_ROTATION_DEADZONE) {
            yaw = Mth.approachDegrees(yaw, targetYaw, sit$getBodyRotationStep(yawDifference));
        }

        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.sit$syncPassengerRotation(passenger);
    }

    @Unique
    private void sit$syncPassengerRotation(Entity passenger) {
        float yaw = this.getYRot();

        if (passenger instanceof LivingEntity living) {
            living.setYBodyRot(yaw);
        }
    }

    @Unique
    private static float sit$getBodyRotationStep(float yawDifference) {
        float normalizedDifference = Mth.clamp(
                (yawDifference - sit$BODY_ROTATION_DEADZONE) / (sit$BODY_ROTATION_FULL_SPEED_AT - sit$BODY_ROTATION_DEADZONE),
                0.0F,
                1.0F
        );
        return Mth.lerp(normalizedDifference, sit$BODY_ROTATION_MIN_STEP, sit$BODY_ROTATION_MAX_STEP);
    }
}
