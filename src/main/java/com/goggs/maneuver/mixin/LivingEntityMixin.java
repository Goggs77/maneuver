package com.goggs.maneuver.mixin;

import com.goggs.maneuver.physics.DamperMath;
import com.goggs.maneuver.physics.DampingTracker;
import com.goggs.maneuver.physics.CeilingFlightTracker;
import com.goggs.maneuver.physics.FallDamageMath;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CeilingFlightTracker {
    @Shadow
    protected abstract float getJumpPower();

    @Unique
    private boolean maneuver$ceilingFlightActive;

    @Unique
    private long maneuver$ceilingFlightTick = Long.MIN_VALUE;

    @Unique
    private long maneuver$ceilingFlightControlTick = Long.MIN_VALUE;

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void maneuver$suppressUpwardFieldJump(CallbackInfo callbackInfo) {
        if ((Object) this instanceof Player player && GravityFieldBlockEntity.suppressesGroundJump(player)) {
            callbackInfo.cancel();
        }
    }

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

    @Override
    public float maneuver$getJumpPower() {
        return getJumpPower();
    }

    @Override
    public boolean maneuver$isCeilingFlightActive(long gameTime) {
        if (!this.maneuver$ceilingFlightActive) {
            return false;
        }
        if (gameTime == this.maneuver$ceilingFlightTick
                || gameTime == this.maneuver$ceilingFlightTick + 1L) {
            return true;
        }
        this.maneuver$ceilingFlightActive = false;
        return false;
    }

    @Override
    public void maneuver$setCeilingFlightActive(boolean active, long gameTime) {
        this.maneuver$ceilingFlightActive = active;
        this.maneuver$ceilingFlightTick = gameTime;
    }

    @Override
    public boolean maneuver$tryBeginCeilingFlightStep(long gameTime) {
        if (this.maneuver$ceilingFlightControlTick == gameTime) {
            return false;
        }
        this.maneuver$ceilingFlightControlTick = gameTime;
        return true;
    }
}

