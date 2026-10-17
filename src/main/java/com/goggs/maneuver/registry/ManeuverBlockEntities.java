package com.goggs.maneuver.registry;

import com.goggs.maneuver.Maneuver;
import com.goggs.maneuver.block.entity.DamperBlockEntity;
import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ManeuverBlockEntities {
    public static final BlockEntityType<GravityFieldBlockEntity> GRAVITY_FIELD = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Maneuver.id("gravity_field"),
            FabricBlockEntityTypeBuilder.create(
                    GravityFieldBlockEntity::new,
                    ManeuverBlocks.GRAVITY_FIELD
            ).build()
    );

    public static final BlockEntityType<DamperBlockEntity> DAMPER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Maneuver.id("damper"),
            FabricBlockEntityTypeBuilder.create(
                    DamperBlockEntity::new,
                    ManeuverBlocks.DAMPER
            ).build()
    );

    private ManeuverBlockEntities() {
    }

    public static void register() {
        // Class initialization performs registry insertion.
    }
}

