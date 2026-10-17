package com.goggs.maneuver;

import com.goggs.maneuver.registry.ManeuverBlockEntities;
import com.goggs.maneuver.registry.ManeuverBlocks;
import com.goggs.maneuver.registry.ManeuverItems;
import com.goggs.maneuver.registry.ManeuverNetworking;
import com.goggs.maneuver.registry.ManeuverScreenHandlers;
import com.goggs.maneuver.physics.FieldRegistry;
import com.goggs.maneuver.physics.ServerFieldManager;
import com.goggs.maneuver.block.entity.DamperBlockEntity;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Maneuver implements ModInitializer {
    public static final String MOD_ID = "maneuver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ManeuverBlocks.register();
        ManeuverBlockEntities.register();
        ManeuverItems.register();
        ManeuverScreenHandlers.register();
        ManeuverNetworking.register();
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof GravityFieldBlockEntity field) {
                FieldRegistry.remove(level, field);
            } else if (blockEntity instanceof DamperBlockEntity damper) {
                FieldRegistry.remove(level, damper);
            }
        });
        ServerFieldManager.register();
        LOGGER.info("Amethyst Maneuver initialized");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

