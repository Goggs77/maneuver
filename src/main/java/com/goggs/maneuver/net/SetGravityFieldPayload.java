package com.goggs.maneuver.net;

import com.goggs.maneuver.Maneuver;
import com.goggs.maneuver.config.GravityFieldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetGravityFieldPayload(BlockPos pos, GravityFieldSettings settings) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetGravityFieldPayload> TYPE =
            new CustomPacketPayload.Type<>(Maneuver.id("set_gravity_field"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGravityFieldPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetGravityFieldPayload::pos,
                    GravityFieldSettings.PACKET_CODEC, SetGravityFieldPayload::settings,
                    SetGravityFieldPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

