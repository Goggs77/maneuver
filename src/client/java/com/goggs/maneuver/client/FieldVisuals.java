package com.goggs.maneuver.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FieldVisuals {
    private static final double PARTICLES_PER_BLOCK = 0.35d;
    private static final int MIN_POINTS_PER_EDGE = 4;
    private static final int SPAWN_INTERVAL_TICKS = 4;

    private FieldVisuals() {
    }

    public static boolean shouldShowBox(Player player, AABB box) {
        Vec3 position = player.position();
        double averageDimension = (
                (box.maxX - box.minX)
                        + (box.maxY - box.minY)
                        + (box.maxZ - box.minZ)
        ) / 3.0d;
        return signedDistanceToBox(box, position) <= averageDimension / 3.0d;
    }

    public static boolean shouldShowSphere(Player player, Vec3 center, double radius) {
        double signedDistance = player.position().distanceTo(center) - radius;
        return signedDistance <= radius * (4.0d / 3.0d);
    }

    public static double signedDistanceToBox(AABB box, Vec3 point) {
        double outsideX = Math.max(Math.max(box.minX - point.x, 0.0d), point.x - box.maxX);
        double outsideY = Math.max(Math.max(box.minY - point.y, 0.0d), point.y - box.maxY);
        double outsideZ = Math.max(Math.max(box.minZ - point.z, 0.0d), point.z - box.maxZ);

        if (outsideX > 0.0d || outsideY > 0.0d || outsideZ > 0.0d) {
            return Math.sqrt(outsideX * outsideX + outsideY * outsideY + outsideZ * outsideZ);
        }

        double inside = Math.min(
                Math.min(point.x - box.minX, box.maxX - point.x),
                Math.min(
                        Math.min(point.y - box.minY, box.maxY - point.y),
                        Math.min(point.z - box.minZ, box.maxZ - point.z)
                )
        );
        return -inside;
    }

    public static void spawnBoxOutline(ClientLevel level, AABB box) {
        long time = level.getGameTime();
        if (time % SPAWN_INTERVAL_TICKS != 0L) {
            return;
        }

        Vec3[] corners = {
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.maxZ)
        };
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (int edge = 0; edge < edges.length; edge++) {
            Vec3 from = corners[edges[edge][0]];
            Vec3 to = corners[edges[edge][1]];
            int points = Math.max(MIN_POINTS_PER_EDGE, (int) Math.ceil(from.distanceTo(to) * PARTICLES_PER_BLOCK));
            for (int i = 0; i < points; i++) {
                double t = (i + 0.5d) / points;
                spawn(level, ParticleTypes.END_ROD, from.lerp(to, t));
            }
        }
    }

    public static void spawnSphereOutline(ClientLevel level, Vec3 center, double radius) {
        long time = level.getGameTime();
        if (time % SPAWN_INTERVAL_TICKS != 0L) {
            return;
        }

        int points = Math.max(
                12,
                (int) Math.ceil(Math.PI * 2.0d * radius * PARTICLES_PER_BLOCK)
        );
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < points; i++) {
                double angle = i * (Math.PI * 2.0d / points);
                double first = Math.cos(angle) * radius;
                double second = Math.sin(angle) * radius;
                Vec3 point = switch (ring) {
                    case 0 -> center.add(first, second, 0.0d);
                    case 1 -> center.add(first, 0.0d, second);
                    default -> center.add(0.0d, first, second);
                };
                spawn(level, ParticleTypes.WITCH, point);
            }
        }
    }

    private static void spawn(ClientLevel level, ParticleOptions particle, Vec3 point) {
        level.addParticle(particle, point.x, point.y, point.z, 0.0d, 0.0d, 0.0d);
    }
}

