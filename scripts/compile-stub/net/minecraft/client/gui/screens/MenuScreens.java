package net.minecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Compile-only facade for the method Fabric's menu API access-widens at runtime.
 */
public final class MenuScreens {
    private MenuScreens() {
    }

    public interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T menu, Inventory inventory, Component title);

        default void fromPacket(Component title, MenuType<T> type, Minecraft minecraft, int containerId) {
        }
    }

    public static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
            MenuType<? extends M> type,
            ScreenConstructor<M, U> constructor
    ) {
    }
}

