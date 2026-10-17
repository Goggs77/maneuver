package com.goggs.maneuver.registry;

import com.goggs.maneuver.Maneuver;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ManeuverItems {
    private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.withDefaultNamespace("functional_blocks")
    );

    public static final Item GRAVITY_FIELD = registerBlockItem("gravity_field", ManeuverBlocks.GRAVITY_FIELD);
    public static final Item DAMPER = registerBlockItem("damper", ManeuverBlocks.DAMPER);

    private ManeuverItems() {
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(GRAVITY_FIELD);
            output.accept(DAMPER);
        });
    }

    private static Item registerBlockItem(String path, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Maneuver.id(path));
        Item.Properties properties = new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(key);
        return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, properties));
    }
}

