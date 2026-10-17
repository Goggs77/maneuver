package com.goggs.maneuver.client;

import com.goggs.maneuver.block.entity.DamperBlockEntity;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import com.goggs.maneuver.physics.FieldRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ClientFieldManager {
    private ClientFieldManager() {
    }

    public static void register() {
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(ClientFieldManager::onUnload);
        ClientTickEvents.END_LEVEL_TICK.register(ClientFieldManager::endLevelTick);
    }

    private static void onUnload(BlockEntity blockEntity, ClientLevel level) {
        if (blockEntity instanceof GravityFieldBlockEntity field) {
            FieldRegistry.remove(level, field);
        } else if (blockEntity instanceof DamperBlockEntity damper) {
            FieldRegistry.remove(level, damper);
        }
    }

    private static void endLevelTick(ClientLevel level) {
        Player player = Minecraft.getInstance().player;

        for (GravityFieldBlockEntity field : FieldRegistry.gravityFields(level)) {
            if (field.isRemoved() || field.getLevel() != level) {
                FieldRegistry.remove(level, field);
                continue;
            }
            field.applyToEntities(level);
            if (player != null && field.getSettings().showRange()
                    && FieldVisuals.shouldShowBox(player, field.getVolumeBox())) {
                FieldVisuals.spawnBoxOutline(level, field.getVolumeBox());
            }
        }

        for (DamperBlockEntity damper : FieldRegistry.dampers(level)) {
            if (damper.isRemoved() || damper.getLevel() != level) {
                FieldRegistry.remove(level, damper);
                continue;
            }
            damper.applyToEntities(level);
            if (player != null && damper.getSettings().showRange()
                    && FieldVisuals.shouldShowSphere(player, damper.getFieldCenter(), damper.getRadius())) {
                FieldVisuals.spawnSphereOutline(level, damper.getFieldCenter(), damper.getRadius());
            }
        }
    }
}

