package com.goggs.maneuver.registry;

import com.goggs.maneuver.block.entity.DamperBlockEntity;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import com.goggs.maneuver.net.SetDamperPayload;
import com.goggs.maneuver.net.SetGravityFieldPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class ManeuverNetworking {
    private static final double MAX_EDIT_DISTANCE_SQUARED = 64.0d * 64.0d;

    private ManeuverNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(SetGravityFieldPayload.TYPE, SetGravityFieldPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetDamperPayload.TYPE, SetDamperPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetGravityFieldPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> applyGravityField(player, payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(SetDamperPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> applyDamper(player, payload));
        });
    }

    private static void applyGravityField(ServerPlayer player, SetGravityFieldPayload payload) {
        if (distanceSquared(player, payload.pos()) > MAX_EDIT_DISTANCE_SQUARED
                || !(player.level().getBlockEntity(payload.pos()) instanceof GravityFieldBlockEntity field)) {
            return;
        }

        field.applySettings(payload.settings());
        player.sendSystemMessage(Component.translatable("message.maneuver.gravity_saved"), true);
    }

    private static void applyDamper(ServerPlayer player, SetDamperPayload payload) {
        if (distanceSquared(player, payload.pos()) > MAX_EDIT_DISTANCE_SQUARED
                || !(player.level().getBlockEntity(payload.pos()) instanceof DamperBlockEntity damper)) {
            return;
        }

        damper.applySettings(payload.settings());
        player.sendSystemMessage(Component.translatable("message.maneuver.damper_saved"), true);
    }

    private static double distanceSquared(ServerPlayer player, net.minecraft.core.BlockPos pos) {
        return player.distanceToSqr(new Vec3(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d));
    }
}
