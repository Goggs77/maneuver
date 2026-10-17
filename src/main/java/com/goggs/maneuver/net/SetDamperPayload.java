package com.goggs.maneuver.net;

import com.goggs.maneuver.Maneuver;
import com.goggs.maneuver.config.DamperSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetDamperPayload(BlockPos pos, DamperSettings settings) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetDamperPayload> TYPE =
            new CustomPacketPayload.Type<>(Maneuver.id("set_damper"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetDamperPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetDamperPayload::pos,
                    DamperSettings.PACKET_CODEC, SetDamperPayload::settings,
                    SetDamperPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

