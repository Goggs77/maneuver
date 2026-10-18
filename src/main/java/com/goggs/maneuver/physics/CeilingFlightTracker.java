package com.goggs.maneuver.physics;

public interface CeilingFlightTracker {
    float maneuver$getJumpPower();

    boolean maneuver$isCeilingFlightActive(long gameTime);

    void maneuver$setCeilingFlightActive(boolean active, long gameTime);

    boolean maneuver$tryBeginCeilingFlightStep(long gameTime);
}

