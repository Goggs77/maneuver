package com.goggs.maneuver.config;

import com.goggs.maneuver.physics.FieldBounds;
import com.goggs.maneuver.physics.GravityVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class PhysicsPacketCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, FieldBounds> BOUNDS =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, FieldBounds::offsetX,
                    ByteBufCodecs.FLOAT, FieldBounds::offsetY,
                    ByteBufCodecs.FLOAT, FieldBounds::offsetZ,
                    ByteBufCodecs.FLOAT, FieldBounds::sizeX,
                    ByteBufCodecs.FLOAT, FieldBounds::sizeY,
                    ByteBufCodecs.FLOAT, FieldBounds::sizeZ,
                    FieldBounds::new
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, GravityVector> GRAVITY =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, GravityVector::x,
                    ByteBufCodecs.FLOAT, GravityVector::y,
                    ByteBufCodecs.FLOAT, GravityVector::z,
                    GravityVector::new
            );

    private PhysicsPacketCodecs() {
    }
}

