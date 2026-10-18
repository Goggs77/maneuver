package com.goggs.maneuver.block.entity;

import com.goggs.maneuver.block.GravityFieldBlock;
import com.goggs.maneuver.config.GravityFieldSettings;
import com.goggs.maneuver.physics.CeilingFlightTracker;
import com.goggs.maneuver.physics.CeilingFlightMath;
import com.goggs.maneuver.physics.DampingTracker;
import com.goggs.maneuver.physics.FieldBounds;
import com.goggs.maneuver.physics.FieldRegistry;
import com.goggs.maneuver.physics.GravityVector;
import com.goggs.maneuver.registry.ManeuverBlockEntities;
import com.goggs.maneuver.screen.GravityFieldData;
import com.goggs.maneuver.screen.GravityFieldScreenHandler;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GravityFieldBlockEntity extends BlockEntity implements ExtendedMenuProvider<GravityFieldData> {
    private FieldBounds bounds = FieldBounds.defaults();
    private GravityVector gravity = GravityVector.normal();
    private boolean showRange = true;
    private boolean affectCreative = false;

    public GravityFieldBlockEntity(BlockPos pos, BlockState state) {
        super(ManeuverBlockEntities.GRAVITY_FIELD, pos, state);
    }

    public void applyToEntities(Level level) {
        if (!getBlockState().getValue(GravityFieldBlock.ACTIVE)) {
            return;
        }

        GravityVector gravity = this.gravity;
        double deltaX = gravity.deltaX();
        double deltaY = gravity.deltaY();
        double deltaZ = gravity.deltaZ();
        if (deltaX == 0.0d && deltaY == 0.0d && deltaZ == 0.0d) {
            return;
        }

        for (Entity entity : level.getEntities(
                (Entity) null,
                getVolumeBox(),
                candidate -> isAffected(candidate, affectCreative)
        )) {
            boolean reversedFlight = handleReversedFlight(level, entity, gravity);
            if (reversedFlight) {
                Vec3 velocity = entity.getDeltaMovement();
                entity.setDeltaMovement(velocity.x + deltaX, velocity.y, velocity.z + deltaZ);
                entity.needsSync = true;
            } else {
                double appliedY = entity.onGround() && gravity.accelerationY() <= 0.0d
                        ? 0.0d
                        : deltaY;
                entity.setDeltaMovement(entity.getDeltaMovement().add(deltaX, appliedY, deltaZ));
                entity.needsSync = true;
            }

            double downwardDelta = -gravity.deltaY();
            if (Math.abs(downwardDelta) > 1.0e-9d) {
                ((DampingTracker) entity).maneuver$addFieldGravity(
                        downwardDelta,
                        level.getGameTime()
                );
            }
        }
    }

    private static boolean handleReversedFlight(Level level, Entity entity, GravityVector gravity) {
        if (gravity.y() < 0.0f || !(entity instanceof Player player) || player.getAbilities().flying) {
            return false;
        }

        CeilingFlightTracker tracker = (CeilingFlightTracker) player;
        long gameTime = level.getGameTime();
        if (!player.isJumping()) {
            tracker.maneuver$setCeilingFlightActive(false, gameTime);
            return false;
        }

        if (gravity.y() > 0.0f && player.onGround()) {
            Vec3 velocity = player.getDeltaMovement();
            if (velocity.y > 0.0d) {
                player.setDeltaMovement(velocity.x, 0.0d, velocity.z);
            }
            tracker.maneuver$setCeilingFlightActive(false, gameTime);
            return true;
        }

        boolean headBlocked = player.verticalCollision && !player.verticalCollisionBelow;
        boolean active = tracker.maneuver$isCeilingFlightActive(gameTime);
        if (!tracker.maneuver$tryBeginCeilingFlightStep(gameTime)) {
            return true;
        }

        Vec3 velocity = player.getDeltaMovement();
        double nextY;
        if (!active && headBlocked) {
            nextY = Math.min(velocity.y, -tracker.maneuver$getJumpPower());
        } else {
            nextY = CeilingFlightMath.nextDescentVelocity(
                    velocity.y,
                    player.getAbilities().getFlyingSpeed()
            );
        }

        player.setDeltaMovement(velocity.x, nextY, velocity.z);
        tracker.maneuver$setCeilingFlightActive(true, gameTime);
        player.needsSync = true;
        return true;
    }

    public static boolean suppressesGroundJump(Player player) {
        for (GravityFieldBlockEntity field : FieldRegistry.gravityFields(player.level())) {
            if (field.getSettings().active()
                    && field.gravity.y() > 0.0f
                    && isAffected(player, field.affectCreative)
                    && field.getVolumeBox().intersects(player.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAffected(Entity entity, boolean affectCreative) {
        if (!entity.isAlive() || entity.isSpectator() || entity.getVehicle() != null) {
            return false;
        }
        return affectCreative || !(entity instanceof Player player && player.isCreative());
    }

    public GravityFieldSettings getSettings() {
        return new GravityFieldSettings(
                getBlockState().getValue(GravityFieldBlock.ACTIVE),
                showRange,
                affectCreative,
                bounds,
                gravity
        );
    }

    public void applySettings(GravityFieldSettings settings) {
        this.bounds = settings.bounds();
        this.gravity = settings.gravity();
        this.showRange = settings.showRange();
        this.affectCreative = settings.affectCreative();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.getValue(GravityFieldBlock.ACTIVE) != settings.active()) {
                level.setBlock(worldPosition, state.setValue(GravityFieldBlock.ACTIVE, settings.active()), Block.UPDATE_ALL);
            }
            BlockState updatedState = getBlockState();
            level.sendBlockUpdated(worldPosition, updatedState, updatedState, Block.UPDATE_ALL);
            setChanged();
        }
    }

    @Override
    public GravityFieldData getScreenOpeningData(ServerPlayer player) {
        return new GravityFieldData(worldPosition, getSettings());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.maneuver.gravity_field");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GravityFieldScreenHandler(
                containerId,
                inventory,
                new GravityFieldData(worldPosition, getSettings())
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("offset_x", bounds.offsetX());
        output.putFloat("offset_y", bounds.offsetY());
        output.putFloat("offset_z", bounds.offsetZ());
        output.putFloat("size_x", bounds.sizeX());
        output.putFloat("size_y", bounds.sizeY());
        output.putFloat("size_z", bounds.sizeZ());
        output.putFloat("gravity_x", gravity.x());
        output.putFloat("gravity_y", gravity.y());
        output.putFloat("gravity_z", gravity.z());
        output.putBoolean("show_range", showRange);
        output.putBoolean("affect_creative", affectCreative);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.bounds = new FieldBounds(
                input.getFloatOr("offset_x", input.getIntOr("offset_x", -4)),
                input.getFloatOr("offset_y", input.getIntOr("offset_y", -2)),
                input.getFloatOr("offset_z", input.getIntOr("offset_z", -4)),
                input.getFloatOr("size_x", input.getIntOr("size_x", 9)),
                input.getFloatOr("size_y", input.getIntOr("size_y", 5)),
                input.getFloatOr("size_z", input.getIntOr("size_z", 9))
        );
        this.gravity = new GravityVector(
                input.getFloatOr("gravity_x", 0.0f),
                input.getFloatOr("gravity_y", -1.0f),
                input.getFloatOr("gravity_z", 0.0f)
        );
        this.showRange = input.getBooleanOr("show_range", true);
        this.affectCreative = input.getBooleanOr("affect_creative", false);
    }

    public AABB getVolumeBox() {
        return new AABB(
                bounds.minX(worldPosition.getX()),
                bounds.minY(worldPosition.getY()),
                bounds.minZ(worldPosition.getZ()),
                bounds.maxX(worldPosition.getX()),
                bounds.maxY(worldPosition.getY()),
                bounds.maxZ(worldPosition.getZ())
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

