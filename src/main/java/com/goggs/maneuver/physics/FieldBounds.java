package com.goggs.maneuver.physics;

/**
 * An axis-aligned rectangular volume expressed relative to its controller.
 * Offsets and sizes are normalized when the record is constructed, so a
 * malformed client payload can never create an unbounded field.
 */
public record FieldBounds(
        float offsetX,
        float offsetY,
        float offsetZ,
        float sizeX,
        float sizeY,
        float sizeZ
) {
    public static final float MIN_OFFSET = -64.0f;
    public static final float MAX_OFFSET = 64.0f;
    public static final float MIN_SIZE = 0.0625f;
    public static final float MAX_SIZE = 96.0f;

    public FieldBounds {
        offsetX = clamp(offsetX, MIN_OFFSET, MAX_OFFSET);
        offsetY = clamp(offsetY, MIN_OFFSET, MAX_OFFSET);
        offsetZ = clamp(offsetZ, MIN_OFFSET, MAX_OFFSET);
        sizeX = clamp(sizeX, MIN_SIZE, MAX_SIZE);
        sizeY = clamp(sizeY, MIN_SIZE, MAX_SIZE);
        sizeZ = clamp(sizeZ, MIN_SIZE, MAX_SIZE);
    }

    public static FieldBounds defaults() {
        return new FieldBounds(-4.0f, -2.0f, -4.0f, 9.0f, 5.0f, 9.0f);
    }

    public double minX(int anchorX) {
        return anchorX + offsetX;
    }

    public double minY(int anchorY) {
        return anchorY + offsetY;
    }

    public double minZ(int anchorZ) {
        return anchorZ + offsetZ;
    }

    public double maxX(int anchorX) {
        return minX(anchorX) + sizeX;
    }

    public double maxY(int anchorY) {
        return minY(anchorY) + sizeY;
    }

    public double maxZ(int anchorZ) {
        return minZ(anchorZ) + sizeZ;
    }

    public double volume() {
        return (double) sizeX * sizeY * sizeZ;
    }

    public boolean contains(int anchorX, int anchorY, int anchorZ, double x, double y, double z) {
        return x >= minX(anchorX) && x < maxX(anchorX)
                && y >= minY(anchorY) && y < maxY(anchorY)
                && z >= minZ(anchorZ) && z < maxZ(anchorZ);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

