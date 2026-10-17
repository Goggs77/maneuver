package com.goggs.maneuver.config;

import com.goggs.maneuver.physics.FieldBounds;
import com.goggs.maneuver.physics.GravityVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GravityFieldSettings(
        boolean active,
        boolean showRange,
        boolean affectCreative,
        FieldBounds bounds,
        GravityVector gravity
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GravityFieldSettings> PACKET_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, GravityFieldSettings::active,
                    ByteBufCodecs.BOOL, GravityFieldSettings::showRange,
                    ByteBufCodecs.BOOL, GravityFieldSettings::affectCreative,
                    PhysicsPacketCodecs.BOUNDS, GravityFieldSettings::bounds,
                    PhysicsPacketCodecs.GRAVITY, GravityFieldSettings::gravity,
                    GravityFieldSettings::new
            );

    public GravityFieldSettings {
        bounds = bounds == null ? FieldBounds.defaults() : bounds;
        gravity = gravity == null ? GravityVector.normal() : gravity;
    }

    public static GravityFieldSettings defaults() {
        return new GravityFieldSettings(true, true, false, FieldBounds.defaults(), GravityVector.normal());
    }

    public GravityFieldSettings withActive(boolean active) {
        return new GravityFieldSettings(active, showRange, affectCreative, bounds, gravity);
    }

    public GravityFieldSettings withShowRange(boolean showRange) {
        return new GravityFieldSettings(active, showRange, affectCreative, bounds, gravity);
    }

    public GravityFieldSettings withAffectCreative(boolean affectCreative) {
        return new GravityFieldSettings(active, showRange, affectCreative, bounds, gravity);
    }
}

