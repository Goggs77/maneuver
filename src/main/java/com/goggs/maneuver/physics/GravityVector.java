package com.goggs.maneuver.physics;

/**
 * Gravity in units of Earth gravity (1 g = Minecraft's 0.08 blocks per tick²).
 */
public record GravityVector(float x, float y, float z) {
    public static final float MAX_COMPONENT = 4.0f;
    public static final float MAX_MAGNITUDE = 4.0f;
    public static final double VANILLA_GRAVITY = 0.08d;

    public GravityVector {
        x = clamp(x, -MAX_COMPONENT, MAX_COMPONENT);
        y = clamp(y, -MAX_COMPONENT, MAX_COMPONENT);
        z = clamp(z, -MAX_COMPONENT, MAX_COMPONENT);

        double magnitude = Math.sqrt(x * x + y * y + z * z);
        if (magnitude > MAX_MAGNITUDE) {
            double scale = MAX_MAGNITUDE / magnitude;
            x = (float) (x * scale);
            y = (float) (y * scale);
            z = (float) (z * scale);
        }
    }

    public static GravityVector normal() {
        return new GravityVector(0.0f, -1.0f, 0.0f);
    }

    public double accelerationX() {
        return x * VANILLA_GRAVITY;
    }

    public double accelerationY() {
        return y * VANILLA_GRAVITY;
    }

    public double accelerationZ() {
        return z * VANILLA_GRAVITY;
    }

    /**
     * Vanilla gravity is still applied by the entity, so fields only add the
     * difference between the desired acceleration and vanilla acceleration.
     */
    public double deltaX() {
        return accelerationX();
    }

    public double deltaY() {
        return accelerationY() - (-VANILLA_GRAVITY);
    }

    public double deltaZ() {
        return accelerationZ();
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

