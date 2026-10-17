package com.goggs.maneuver.mixin;

import com.goggs.maneuver.physics.DamperMath;
import com.goggs.maneuver.physics.DampingTracker;
import com.goggs.maneuver.physics.FallDamageMath;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(
            method = "calculateFallDamage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = true
    )
    private double maneuver$scaleFallDistance(double fallDistance) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DampingTracker tracker = (DampingTracker) (Object) this;
        double removed = tracker.maneuver$getRemovedFallSquaredSpeed();
        double current = tracker.maneuver$getLastFallSquaredSpeed();
        double fraction = DamperMath.protectedFraction(removed, current);
        double effectiveDistance = fallDistance;

        long currentTick = entity.level().getGameTime();
        long gravityTick = tracker.maneuver$getFieldGravityTick();
        if (gravityTick == currentTick || gravityTick == currentTick - 1L) {
            double impactSpeed = Math.sqrt(Math.max(0.0d, current + removed));
            double acceleration = FallDamageMath.netDownwardAcceleration(
                    tracker.maneuver$getFieldGravity()
            );
            effectiveDistance = FallDamageMath.equivalentFallDistance(impactSpeed, acceleration);
        }

        tracker.maneuver$resetFallDamping();
        tracker.maneuver$resetFieldGravity();

        return effectiveDistance * (1.0d - fraction);
    }
}

