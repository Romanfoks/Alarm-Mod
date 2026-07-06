package com.romanfiks.alarmmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AlarmBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);
    public static final MapCodec<AlarmBlock> CODEC = simpleCodec(AlarmBlock::new);

    public AlarmBlock(Properties properties) {
        super(properties);
    }
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /* BLOCK ENTITY */

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlarmBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            updateAlarm(level, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!state.is(oldState.getBlock()) && !level.isClientSide) {
            updateAlarm(level, pos);
        }
    }

    public static void updateAlarm(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AlarmBlockEntity be) {
            boolean powered = level.hasNeighborSignal(pos);
            if (!powered) {
                BlockState below = level.getBlockState(pos.below());
                if (below.is(Blocks.REDSTONE_WIRE)) {
                    powered = below.getValue(RedStoneWireBlock.POWER) > 0;
                }
            }
            be.setAlarmOn(powered);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
