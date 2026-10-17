package com.goggs.maneuver.mixin;

import com.goggs.maneuver.physics.DampingTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements DampingTracker {
    @Unique
    private double maneuver$removedFallSquaredSpeed;

    @Unique
    private double maneuver$lastFallSquaredSpeed;

    @Unique
    private double maneuver$fieldGravity;

    @Unique
    private long maneuver$fieldGravityTick = Long.MIN_VALUE;

    @Inject(method = "tick", at = @At("HEAD"))
    private void maneuver$captureFallSpeed(CallbackInfo callbackInfo) {
        Entity entity = (Entity) (Object) this;
        double verticalSpeed = entity.getDeltaMovement().y;
        this.maneuver$lastFallSquaredSpeed = verticalSpeed * verticalSpeed;

        if (entity.onGround()) {
            this.maneuver$removedFallSquaredSpeed = 0.0d;
            this.maneuver$resetFieldGravity();
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void maneuver$captureImpactSpeed(
            MoverType movementType,
            Vec3 movement,
            CallbackInfo callbackInfo
    ) {
        if (movement.y < 0.0d) {
            this.maneuver$lastFallSquaredSpeed = movement.y * movement.y;
        }
    }

    @Override
    public void maneuver$addRemovedFallSquaredSpeed(double removedSquaredSpeed) {
        if (removedSquaredSpeed > 0.0d) {
            this.maneuver$removedFallSquaredSpeed += removedSquaredSpeed;
        }
    }

    @Override
    public double maneuver$getRemovedFallSquaredSpeed() {
        return this.maneuver$removedFallSquaredSpeed;
    }

    @Override
    public double maneuver$getLastFallSquaredSpeed() {
        return this.maneuver$lastFallSquaredSpeed;
    }

    @Override
    public void maneuver$resetFallDamping() {
        this.maneuver$removedFallSquaredSpeed = 0.0d;
    }

    @Override
    public void maneuver$addFieldGravity(double downwardDelta, long gameTime) {
        if (this.maneuver$fieldGravityTick != gameTime) {
            this.maneuver$fieldGravity = 0.0d;
            this.maneuver$fieldGravityTick = gameTime;
        }
        this.maneuver$fieldGravity += downwardDelta;
    }

    @Override
    public double maneuver$getFieldGravity() {
        return this.maneuver$fieldGravity;
    }

    @Override
    public long maneuver$getFieldGravityTick() {
        return this.maneuver$fieldGravityTick;
    }

    @Override
    public void maneuver$resetFieldGravity() {
        this.maneuver$fieldGravity = 0.0d;
        this.maneuver$fieldGravityTick = Long.MIN_VALUE;
    }
}
