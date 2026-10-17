package com.goggs.maneuver.screen;

import com.goggs.maneuver.config.DamperSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DamperData(BlockPos pos, DamperSettings settings) {
    public static final StreamCodec<RegistryFriendlyByteBuf, DamperData> PACKET_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, DamperData::pos,
                    DamperSettings.PACKET_CODEC, DamperData::settings,
                    DamperData::new
            );
}
