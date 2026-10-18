package com.goggs.maneuver.block.entity;

import com.goggs.maneuver.block.DamperBlock;
import com.goggs.maneuver.config.DamperSettings;
import com.goggs.maneuver.physics.DamperMath;
import com.goggs.maneuver.physics.DampingTracker;
import com.goggs.maneuver.registry.ManeuverBlockEntities;
import com.goggs.maneuver.screen.DamperData;
import com.goggs.maneuver.screen.DamperScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DamperBlockEntity extends BlockEntity implements ExtendedMenuProvider<DamperData> {
    private float radius = 6.0f;
    private float strength = 0.55f;
    private boolean showRange = true;
    private boolean affectCreative = false;

    public DamperBlockEntity(BlockPos pos, BlockState state) {
        super(ManeuverBlockEntities.DAMPER, pos, state);
    }

    public void applyToEntities(Level level) {
        if (!getBlockState().getValue(DamperBlock.ACTIVE) || strength <= 0.0f) {
            return;
        }

        Vec3 center = getFieldCenter();
        for (Entity entity : level.getEntities(
                (Entity) null,
                getSearchBox(),
                candidate -> isAffected(candidate, affectCreative)
        )) {
            double factor = DamperMath.velocityFactor(
                    strength,
                    entity.position().distanceTo(center),
                    radius
            );
            if (factor >= 0.999999d) {
                continue;
            }

            Vec3 oldVelocity = entity.getDeltaMovement();
            Vec3 newVelocity = oldVelocity.multiply(factor, factor, factor);
            if (oldVelocity.y < 0.0d) {
                ((DampingTracker) entity).maneuver$addRemovedFallSquaredSpeed(
                        oldVelocity.y * oldVelocity.y - newVelocity.y * newVelocity.y
                );
            }
            entity.setDeltaMovement(newVelocity);
            entity.needsSync = true;
        }
    }

    private static boolean isAffected(Entity entity, boolean affectCreative) {
        if (!entity.isAlive() || entity.isSpectator() || entity.getVehicle() != null) {
            return false;
        }
        return affectCreative || !(entity instanceof Player player && player.isCreative());
    }

    public DamperSettings getSettings() {
        return new DamperSettings(
                radius,
                strength,
                getBlockState().getValue(DamperBlock.ACTIVE),
                showRange,
                affectCreative
        );
    }

    public void applySettings(DamperSettings settings) {
        this.radius = settings.radius();
        this.strength = settings.strength();
        this.showRange = settings.showRange();
        this.affectCreative = settings.affectCreative();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.getValue(DamperBlock.ACTIVE) != settings.active()) {
                level.setBlock(worldPosition, state.setValue(DamperBlock.ACTIVE, settings.active()), Block.UPDATE_ALL);
            }
            BlockState updatedState = getBlockState();
            level.sendBlockUpdated(worldPosition, updatedState, updatedState, Block.UPDATE_ALL);
            setChanged();
        }
    }

    @Override
    public DamperData getScreenOpeningData(ServerPlayer player) {
        return new DamperData(worldPosition, getSettings());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.maneuver.damper");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DamperScreenHandler(
                containerId,
                inventory,
                new DamperData(worldPosition, getSettings())
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("radius", radius);
        output.putFloat("strength", strength);
        output.putBoolean("show_range", showRange);
        output.putBoolean("affect_creative", affectCreative);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.radius = input.getFloatOr("radius", input.getIntOr("radius", 6));
        this.strength = input.getFloatOr("strength", 0.55f);
        this.showRange = input.getBooleanOr("show_range", true);
        this.affectCreative = input.getBooleanOr("affect_creative", false);
    }

    public Vec3 getFieldCenter() {
        return new Vec3(
                worldPosition.getX() + 0.5d,
                worldPosition.getY() + 0.5d,
                worldPosition.getZ() + 0.5d
        );
    }

    public double getRadius() {
        return radius;
    }

    public AABB getSearchBox() {
        Vec3 center = getFieldCenter();
        return new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
