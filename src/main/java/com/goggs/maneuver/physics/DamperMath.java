package com.goggs.maneuver.physics;

public final class DamperMath {
    public static final double MAX_COEFFICIENT_PER_SECOND = 3.5d;
    public static final double VANILLA_TICK_SECONDS = 0.05d;

    private DamperMath() {
    }

    /**
     * Converts a 0..1 strength into an exponential damping coefficient.
     * Velocity follows v(t) = v(0) * exp(-coefficient * falloff * t).
     */
    public static double coefficient(double normalizedStrength) {
        double strength = Math.max(0.0d, Math.min(1.0d, normalizedStrength));
        return strength * MAX_COEFFICIENT_PER_SECOND;
    }

    public static double radialFalloff(double distance, double radius) {
        if (radius <= 0.0d || distance >= radius) {
            return 0.0d;
        }
        return 1.0d - Math.max(0.0d, distance) / radius;
    }

    public static double velocityFactor(double normalizedStrength, double distance, double radius) {
        double falloff = radialFalloff(distance, radius);
        return Math.exp(-coefficient(normalizedStrength) * falloff * VANILLA_TICK_SECONDS);
    }

    /**
     * Fraction of a fall's kinetic proxy removed by damping. The remaining
     * current squared speed keeps this from producing an infinite reduction.
     */
    public static double protectedFraction(double removedSquaredSpeed, double currentSquaredSpeed) {
        if (removedSquaredSpeed <= 0.0d) {
            return 0.0d;
        }
        double current = Math.max(0.0d, currentSquaredSpeed);
        return removedSquaredSpeed / (removedSquaredSpeed + current);
    }
}

