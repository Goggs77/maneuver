package com.goggs.maneuver.physics;

public interface DampingTracker {
    void maneuver$addRemovedFallSquaredSpeed(double removedSquaredSpeed);

    double maneuver$getRemovedFallSquaredSpeed();

    double maneuver$getLastFallSquaredSpeed();

    void maneuver$resetFallDamping();

    void maneuver$addFieldGravity(double downwardDelta, long gameTime);

    double maneuver$getFieldGravity();

    long maneuver$getFieldGravityTick();

    void maneuver$resetFieldGravity();
}

