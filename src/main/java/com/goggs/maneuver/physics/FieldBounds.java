package com.goggs.maneuver.physics;

/**
 * An axis-aligned rectangular volume expressed relative to its controller.
 * Offsets and sizes are normalized when the record is constructed, so a
 * malformed client payload can never create an unbounded field.
 */
public record FieldBounds(
        int offsetX,
        int offsetY,
        int offsetZ,
        int sizeX,
        int sizeY,
        int sizeZ
) {
    public static final int MIN_OFFSET = -64;
    public static final int MAX_OFFSET = 64;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 96;

    public FieldBounds {
        offsetX = clamp(offsetX, MIN_OFFSET, MAX_OFFSET);
        offsetY = clamp(offsetY, MIN_OFFSET, MAX_OFFSET);
        offsetZ = clamp(offsetZ, MIN_OFFSET, MAX_OFFSET);
        sizeX = clamp(sizeX, MIN_SIZE, MAX_SIZE);
        sizeY = clamp(sizeY, MIN_SIZE, MAX_SIZE);
        sizeZ = clamp(sizeZ, MIN_SIZE, MAX_SIZE);
    }

    public static FieldBounds defaults() {
        return new FieldBounds(-4, -2, -4, 9, 5, 9);
    }

    public int minX(int anchorX) {
        return anchorX + offsetX;
    }

    public int minY(int anchorY) {
        return anchorY + offsetY;
    }

    public int minZ(int anchorZ) {
        return anchorZ + offsetZ;
    }

    public int maxX(int anchorX) {
        return minX(anchorX) + sizeX;
    }

    public int maxY(int anchorY) {
        return minY(anchorY) + sizeY;
    }

    public int maxZ(int anchorZ) {
        return minZ(anchorZ) + sizeZ;
    }

    public long volume() {
        return (long) sizeX * sizeY * sizeZ;
    }

    public boolean contains(int anchorX, int anchorY, int anchorZ, double x, double y, double z) {
        return x >= minX(anchorX) && x < maxX(anchorX)
                && y >= minY(anchorY) && y < maxY(anchorY)
                && z >= minZ(anchorZ) && z < maxZ(anchorZ);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

