package com.romanfiks.alarmmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlarmBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final MapCodec<AlarmBlock> CODEC = simpleCodec(AlarmBlock::new);

    public AlarmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }
    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }
    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Using single shape for all facings for now; can be customized per facing if desired
        return SHAPE;
    }
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /* BLOCK ENTITY */

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, face);
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AlarmBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            updateAlarm(level, pos);
        }
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        if (!state.is(oldState.getBlock()) && !level.isClientSide) {
            updateAlarm(level, pos);
        }
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);
        if (!level.isClientSide) {
            updateAlarm(level, pos);
        }
    }

    public static void updateAlarm(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AlarmBlockEntity be) {
            int redstoneSignal = getRedstoneSignal(level, pos);
            be.setRedstoneSignal(redstoneSignal);
        }
    }

    public static int getRedstoneSignal(Level level, BlockPos pos) {
        Direction facing = level.getBlockState(pos).getValue(FACING);

        int neighborPower = 0;
        for (Direction direction : perpendiculars(facing)) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.isSignalSource()) {
                neighborPower = Math.max(neighborPower, neighborState.getSignal(level, neighborPos, direction.getOpposite()));
            }
        }
        System.out.println(neighborPower);

        BlockPos basePos = pos.relative(facing.getOpposite());
        return Math.max(level.getSignal(basePos,Direction.DOWN),neighborPower);
    }

    protected static Direction[] perpendiculars(Direction direction) {
        return switch (direction) {
            case DOWN, UP -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            case NORTH, SOUTH -> new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.SOUTH};
            case EAST, WEST -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.SOUTH};
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
