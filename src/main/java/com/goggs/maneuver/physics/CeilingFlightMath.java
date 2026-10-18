package com.goggs.maneuver.physics;

public final class CeilingFlightMath {
    public static final double VERTICAL_DRAG = 0.6d;
    public static final double FLIGHT_ACCELERATION_MULTIPLIER = 3.0d;

    private CeilingFlightMath() {
    }

    public static double nextDescentVelocity(double currentY, double flyingSpeed) {
        double acceleration = Math.max(0.0d, flyingSpeed) * FLIGHT_ACCELERATION_MULTIPLIER;
        return VERTICAL_DRAG * (currentY - acceleration);
    }

    public static double terminalDescentVelocity(double flyingSpeed) {
        double acceleration = Math.max(0.0d, flyingSpeed) * FLIGHT_ACCELERATION_MULTIPLIER;
        return -VERTICAL_DRAG * acceleration / (1.0d - VERTICAL_DRAG);
    }
}

