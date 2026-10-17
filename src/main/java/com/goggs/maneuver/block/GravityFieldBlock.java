package com.goggs.maneuver.block;

import com.goggs.maneuver.block.entity.GravityFieldBlockEntity;
import com.goggs.maneuver.physics.FieldRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class GravityFieldBlock extends BaseEntityBlock {
    public static final MapCodec<GravityFieldBlock> CODEC = simpleCodec(GravityFieldBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public GravityFieldBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravityFieldBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof GravityFieldBlockEntity field) {
                FieldRegistry.register(tickLevel, field);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof GravityFieldBlockEntity field) {
            serverPlayer.openMenu(field);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.8f, 1.2f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE) || random.nextInt(7) != 0) {
            return;
        }
        double x = pos.getX() + 0.2d + random.nextDouble() * 0.6d;
        double y = pos.getY() + 0.2d + random.nextDouble() * 0.6d;
        double z = pos.getZ() + 0.2d + random.nextDouble() * 0.6d;
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0d, 0.008d, 0.0d);
    }
}

