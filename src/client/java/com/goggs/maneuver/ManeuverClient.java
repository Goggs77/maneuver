package com.goggs.maneuver;

import com.goggs.maneuver.client.GravityFieldScreen;
import com.goggs.maneuver.client.DamperScreen;
import com.goggs.maneuver.client.ClientFieldManager;
import com.goggs.maneuver.registry.ManeuverScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class ManeuverClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ManeuverScreenHandlers.GRAVITY_FIELD, GravityFieldScreen::new);
        MenuScreens.register(ManeuverScreenHandlers.DAMPER, DamperScreen::new);
        ClientFieldManager.register();
    }
}

