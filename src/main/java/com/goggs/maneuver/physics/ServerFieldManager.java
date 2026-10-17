package com.goggs.maneuver.physics;

import com.goggs.maneuver.block.entity.DamperBlockEntity;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

public final class ServerFieldManager {
    private ServerFieldManager() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(ServerFieldManager::endLevelTick);
    }

    private static void endLevelTick(ServerLevel level) {
        for (GravityFieldBlockEntity field : FieldRegistry.gravityFields(level)) {
            if (field.isRemoved() || field.getLevel() != level) {
                FieldRegistry.remove(level, field);
            } else {
                field.applyToEntities(level);
            }
        }

        for (DamperBlockEntity damper : FieldRegistry.dampers(level)) {
            if (damper.isRemoved() || damper.getLevel() != level) {
                FieldRegistry.remove(level, damper);
            } else {
                damper.applyToEntities(level);
            }
        }
    }
}

