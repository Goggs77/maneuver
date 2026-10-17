package com.goggs.maneuver.registry;

import com.goggs.maneuver.Maneuver;
import com.goggs.maneuver.screen.DamperData;
import com.goggs.maneuver.screen.DamperScreenHandler;
import com.goggs.maneuver.screen.GravityFieldData;
import com.goggs.maneuver.screen.GravityFieldScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class ManeuverScreenHandlers {
    public static final MenuType<GravityFieldScreenHandler> GRAVITY_FIELD = Registry.register(
            BuiltInRegistries.MENU,
            Maneuver.id("gravity_field"),
            new ExtendedMenuType<>(GravityFieldScreenHandler::new, GravityFieldData.PACKET_CODEC)
    );

    public static final MenuType<DamperScreenHandler> DAMPER = Registry.register(
            BuiltInRegistries.MENU,
            Maneuver.id("damper"),
            new ExtendedMenuType<>(DamperScreenHandler::new, DamperData.PACKET_CODEC)
    );

    private ManeuverScreenHandlers() {
    }

    public static void register() {
        // Class initialization performs registry insertion.
    }
}
