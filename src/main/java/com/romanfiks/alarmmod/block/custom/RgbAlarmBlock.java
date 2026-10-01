package com.romanfiks.alarmmod.block.custom;

import com.mojang.serialization.MapCodec;
import com.romanfiks.alarmmod.block.entity.RgbAlarmBlockEntity;
import com.romanfiks.alarmmod.block.entity.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.createmod.catnip.math.VoxelShaper;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.SurfaceElectricBlock;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.base.terminals.BlockStateTerminalCollection;
import org.patryk3211.powergrid.electricity.info.IHaveElectricProperties;
import org.patryk3211.powergrid.electricity.info.Voltage;

import java.util.List;

/**
 * RGB-сирена: электроприбор Power Grid с четырьмя контактами на стороне крепления —
 * красный, зелёный, синий и общий минус.
 *
 * <p>Ряд контактов всегда лежит в одном и том же месте относительно поверхности
 * крепления и не поворачивается вслед за {@code ALONG_FIRST_AXIS}: свойство
 * досталось по наследству от {@link SurfaceElectricBlock}, но на раскладку контактов
 * оно не влияет. Блокстейт собран на {@code multipart} по одному лишь {@code FACING}.
 */
public class RgbAlarmBlock extends SurfaceElectricBlock implements IBE<RgbAlarmBlockEntity>, IHaveElectricProperties {

    public static final MapCodec<RgbAlarmBlock> CODEC = simpleCodec(RgbAlarmBlock::new);

    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);

    /** Контакты заданы для блока, смотрящего вниз, и разворачиваются по {@code FACING}. */
    private static final TerminalBoundingBox[] TERMINALS_DOWN = {
        new TerminalBoundingBox(colorName("terminal.alarmmod.red", ChatFormatting.RED), 2, 0, 6, 5, 2, 10)
            .withColor(IDecoratedTerminal.RED),
        new TerminalBoundingBox(colorName("terminal.alarmmod.green", ChatFormatting.GREEN), 5, 0, 6, 8, 2, 10)
            .withColor(IDecoratedTerminal.GREEN),
        new TerminalBoundingBox(colorName("terminal.alarmmod.blue", ChatFormatting.BLUE), 8, 0, 6, 11, 2, 10)
            .withColor(IDecoratedTerminal.BLUE),
        new TerminalBoundingBox(IDecoratedTerminal.NEGATIVE, 11, 0, 6, 14, 2, 10)
            .withColor(IDecoratedTerminal.GRAY)
    };

    public RgbAlarmBlock(Properties properties) {
        super(properties);
        var shaper = VoxelShaper.forDirectional(SHAPE, Direction.DOWN);
        setTerminalCollection(BlockStateTerminalCollection.builder(this)
            .forAllStates(state -> BlockStateTerminalCollection.each(TERMINALS_DOWN, terminal -> {
                var facing = state.getValue(FACING);
                return switch (facing) {
                    case DOWN -> terminal;
                    case UP -> terminal.rotateAroundX(180);
                    case EAST -> terminal.rotateAroundZ(90).rotateAroundY(180);
                    case WEST -> terminal.rotateAroundZ(90);
                    case NORTH -> terminal.rotateAroundZ(90).rotateAroundY(90);
                    case SOUTH -> terminal.rotateAroundZ(90).rotateAroundY(-90);
                };
            }))
            .withShapeMapper(state -> shaper.get(state.getValue(FACING)))
            .build());
    }

    private static Component colorName(String key, ChatFormatting style) {
        return Component.translatable(key).withStyle(style);
    }

    @Override
    public int terminalCount() {
        return RgbAlarmBlockEntity.TERMINAL_COUNT;
    }

    /**
     * Направление, в котором сирена светит. Проверено в игре: {@code FACING} у
     * SurfaceElectricBlock указывает наружу от поверхности крепления, поэтому свет
     * направлен именно в эту сторону, без инверсии.
     */
    public static Direction lightDirection(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    protected MapCodec<? extends SurfaceElectricBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<RgbAlarmBlockEntity> getBlockEntityClass() {
        return RgbAlarmBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RgbAlarmBlockEntity> getBlockEntityType() {
        return ModBlockEntities.RGB_ALARM_BE.get();
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        Voltage.rated(RgbAlarmBlockEntity.RATED_CHANNEL_VOLTAGE, player, tooltip);
    }
}
