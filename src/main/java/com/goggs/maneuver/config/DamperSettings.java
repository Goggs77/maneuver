package com.goggs.maneuver.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DamperSettings(
        float radius,
        float strength,
        boolean active,
        boolean showRange,
        boolean affectCreative
) {
    public static final float MIN_RADIUS = 0.5f;
    public static final float MAX_RADIUS = 32.0f;

    public static final StreamCodec<RegistryFriendlyByteBuf, DamperSettings> PACKET_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, DamperSettings::radius,
                    ByteBufCodecs.FLOAT, DamperSettings::strength,
                    ByteBufCodecs.BOOL, DamperSettings::active,
                    ByteBufCodecs.BOOL, DamperSettings::showRange,
                    ByteBufCodecs.BOOL, DamperSettings::affectCreative,
                    DamperSettings::new
            );

    public DamperSettings {
        radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
        strength = Math.max(0.0f, Math.min(1.0f, strength));
    }

    public static DamperSettings defaults() {
        return new DamperSettings(6.0f, 0.55f, true, true, false);
    }

    public DamperSettings withActive(boolean active) {
        return new DamperSettings(radius, strength, active, showRange, affectCreative);
    }

    public DamperSettings withShowRange(boolean showRange) {
        return new DamperSettings(radius, strength, active, showRange, affectCreative);
    }

    public DamperSettings withAffectCreative(boolean affectCreative) {
        return new DamperSettings(radius, strength, active, showRange, affectCreative);
    }
}

