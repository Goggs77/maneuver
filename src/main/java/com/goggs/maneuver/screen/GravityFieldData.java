package com.goggs.maneuver.screen;

import com.goggs.maneuver.config.GravityFieldSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record GravityFieldData(BlockPos pos, GravityFieldSettings settings) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GravityFieldData> PACKET_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, GravityFieldData::pos,
                    GravityFieldSettings.PACKET_CODEC, GravityFieldData::settings,
                    GravityFieldData::new
            );
}

