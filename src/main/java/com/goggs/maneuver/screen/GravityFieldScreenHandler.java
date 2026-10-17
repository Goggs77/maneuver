package com.goggs.maneuver.screen;

import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import com.goggs.maneuver.config.GravityFieldSettings;
import com.goggs.maneuver.registry.ManeuverScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class GravityFieldScreenHandler extends AbstractContainerMenu {
    private final BlockPos pos;
    private GravityFieldSettings settings;

    public GravityFieldScreenHandler(int syncId, Inventory playerInventory, GravityFieldData data) {
        super(ManeuverScreenHandlers.GRAVITY_FIELD, syncId);
        this.pos = data.pos();
        this.settings = data.settings();
    }

    public BlockPos getPos() {
        return pos;
    }

    public GravityFieldSettings getSettings() {
        return settings;
    }

    public void setClientSettings(GravityFieldSettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(new Vec3(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d)) <= 64.0d * 64.0d
                && player.level().getBlockEntity(pos) instanceof GravityFieldBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}

