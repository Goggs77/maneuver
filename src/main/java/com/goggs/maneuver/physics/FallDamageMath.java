package com.goggs.maneuver.physics;

public final class FallDamageMath {
    public static final double AIR_DRAG = 0.98d;
    public static final double DEFAULT_DOWNWARD_ACCELERATION = GravityVector.VANILLA_GRAVITY;
    public static final double EPSILON = 1.0e-6d;

    private FallDamageMath() {
    }

    /**
     * Exact Minecraft 26.2 fall damage formula before its damage immunity check.
     */
    public static int vanillaFallDamage(
            double fallDistance,
            float damageMultiplier,
            double safeFallDistance,
            double fallDamageMultiplier
    ) {
        double power = fallDistance + EPSILON - safeFallDistance;
        return (int) Math.floor(power * damageMultiplier * fallDamageMultiplier);
    }

    public static double terminalVelocity(double downwardAcceleration) {
        double acceleration = validAcceleration(downwardAcceleration);
        return AIR_DRAG * acceleration / (1.0d - AIR_DRAG);
    }

    /**
     * Inverts Minecraft's discrete gravity-plus-drag motion. Starting from rest,
     * v[n] = terminal * (1 - drag^n), so an impact speed can be mapped back to
     * the fall distance that would have produced it under the current
     * acceleration.
     */
    public static double equivalentFallDistance(double impactSpeed, double downwardAcceleration) {
        double speed = Math.abs(impactSpeed);
        if (speed <= EPSILON) {
            return 0.0d;
        }

        double acceleration = validAcceleration(downwardAcceleration);
        double terminal = terminalVelocity(acceleration);
        if (speed >= terminal) {
            // The discrete terminal-speed equation has no finite inverse here.
            return speed * speed / (2.0d * acceleration);
        }

        double ticks = Math.log1p(-speed / terminal) / Math.log(AIR_DRAG);
        double distance = terminal * ticks - AIR_DRAG * speed / (1.0d - AIR_DRAG);
        return Math.max(0.0d, distance);
    }

    public static double netDownwardAcceleration(double fieldGravityDelta) {
        return DEFAULT_DOWNWARD_ACCELERATION + fieldGravityDelta;
    }

    private static double validAcceleration(double acceleration) {
        return acceleration > EPSILON ? acceleration : DEFAULT_DOWNWARD_ACCELERATION;
    }
}

