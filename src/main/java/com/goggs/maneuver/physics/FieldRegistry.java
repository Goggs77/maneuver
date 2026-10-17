package com.goggs.maneuver.physics;

import com.goggs.maneuver.block.entity.DamperBlockEntity;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class FieldRegistry {
    private static final Map<Level, Bucket> BUCKETS = new WeakHashMap<>();

    private FieldRegistry() {
    }

    public static void register(Level level, GravityFieldBlockEntity field) {
        bucket(level).gravity.add(field);
    }

    public static void register(Level level, DamperBlockEntity damper) {
        bucket(level).dampers.add(damper);
    }

    public static GravityFieldBlockEntity[] gravityFields(Level level) {
        return bucket(level).gravity.toArray(GravityFieldBlockEntity[]::new);
    }

    public static DamperBlockEntity[] dampers(Level level) {
        return bucket(level).dampers.toArray(DamperBlockEntity[]::new);
    }

    public static void remove(Level level, GravityFieldBlockEntity field) {
        Bucket bucket = BUCKETS.get(level);
        if (bucket != null) {
            bucket.gravity.remove(field);
            removeIfEmpty(level, bucket);
        }
    }

    public static void remove(Level level, DamperBlockEntity damper) {
        Bucket bucket = BUCKETS.get(level);
        if (bucket != null) {
            bucket.dampers.remove(damper);
            removeIfEmpty(level, bucket);
        }
    }

    private static void removeIfEmpty(Level level, Bucket bucket) {
        if (bucket.gravity.isEmpty() && bucket.dampers.isEmpty()) {
            synchronized (BUCKETS) {
                BUCKETS.remove(level);
            }
        }
    }

    private static Bucket bucket(Level level) {
        synchronized (BUCKETS) {
            return BUCKETS.computeIfAbsent(level, ignored -> new Bucket());
        }
    }

    private static final class Bucket {
        private final Set<GravityFieldBlockEntity> gravity =
                Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<DamperBlockEntity> dampers =
                Collections.newSetFromMap(new IdentityHashMap<>());
    }
}

