package com.goggs.maneuver.registry;

import com.goggs.maneuver.Maneuver;
import com.goggs.maneuver.block.DamperBlock;
import com.goggs.maneuver.block.GravityFieldBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ManeuverBlocks {
    public static final ResourceKey<Block> GRAVITY_FIELD_KEY = key("gravity_field");
    public static final ResourceKey<Block> DAMPER_KEY = key("damper");

    public static final Block GRAVITY_FIELD = Registry.register(
            BuiltInRegistries.BLOCK,
            GRAVITY_FIELD_KEY,
            new GravityFieldBlock(BlockBehaviour.Properties.of()
                    .setId(GRAVITY_FIELD_KEY)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(GravityFieldBlock.ACTIVE) ? 5 : 1))
    );

    public static final Block DAMPER = Registry.register(
            BuiltInRegistries.BLOCK,
            DAMPER_KEY,
            new DamperBlock(BlockBehaviour.Properties.of()
                    .setId(DAMPER_KEY)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(DamperBlock.ACTIVE) ? 3 : 1))
    );

    private ManeuverBlocks() {
    }

    public static void register() {
        // Class initialization performs registry insertion.
    }

    private static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK, Maneuver.id(path));
    }
}

