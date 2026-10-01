package com.romanfiks.alarmmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;
import org.patryk3211.powergrid.electricity.sim.node.FloatingNode;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * RGB-светодиод для электросети Power Grid.
 *
 * <p>Четыре контакта: три цветовых канала и общий минус. Каждый цветовой канал — это
 * отдельное сопротивление от своего контакта к общему узлу минуса, то есть буквально
 * светодиод с общим катодом. Цвет получается сам собой из схемы подключения:
 * запитан только R — красный, R и G — жёлтый, все три — белый.
 *
 * <p>Яркость канала аналоговая и зависит от напряжения на нём, поэтому прибор можно
 * приглушать реостатом или понижающим трансформатором.
 */
public class RgbAlarmBlockEntity extends ElectricBlockEntity implements IElectricEntity {

    public static final int TERMINAL_RED = 0;
    public static final int TERMINAL_GREEN = 1;
    public static final int TERMINAL_BLUE = 2;
    public static final int TERMINAL_GROUND = 3;
    public static final int TERMINAL_COUNT = 4;

    /**
     * Сопротивление одного цветового канала. Три канала включены параллельно, поэтому
     * суммарное сопротивление прибора — примерно треть этого значения.
     */
    public static final float CHANNEL_RESISTANCE = 100f;

    /** Напряжение, при котором канал считается полностью выведенным на яркость. */
    public static final float RATED_CHANNEL_VOLTAGE = 32f;

    /** Ниже этого напряжения канал считается выключенным — отсекает дребезг при нуле. */
    public static final float CHANNEL_ON_THRESHOLD = 0.5f;

    // Клиентские колбэки (устанавливаются в AlarmClientHandler).
    public static Consumer<RgbAlarmBlockEntity> onClientLoad;
    public static Consumer<RgbAlarmBlockEntity> onClientRemove;
    public static BiConsumer<BlockPos, RgbAlarmBlockEntity> onClientChanged;

    private ElectricWire redWire;
    private ElectricWire greenWire;
    private ElectricWire blueWire;

    private float red;
    private float green;
    private float blue;

    /**
     * NeoForge создаёт сущности через {@code BlockEntitySupplier}, который передаёт только
     * позицию и состояние. Тип берём из уже зарегистрированного supplier: к моменту
     * создания сущности регистрация давно завершена, так что цикла инициализации нет.
     */
    public RgbAlarmBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RGB_ALARM_BE.get(), pos, state);
    }

    public RgbAlarmBlockEntity(BlockEntityType<RgbAlarmBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Схема прибора: три цветовых канала от своих контактов к общему минусу.

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(TERMINAL_COUNT);
        FloatingNode ground = builder.terminalNode(TERMINAL_GROUND);
        redWire = builder.connect(CHANNEL_RESISTANCE, builder.terminalNode(TERMINAL_RED), ground);
        greenWire = builder.connect(CHANNEL_RESISTANCE, builder.terminalNode(TERMINAL_GREEN), ground);
        blueWire = builder.connect(CHANNEL_RESISTANCE, builder.terminalNode(TERMINAL_BLUE), ground);
    }

    @Override
    public void electricalTick() {
        applyPower(redWire);
        applyPower(greenWire);
        applyPower(blueWire);

        float nextRed = channelLevel(redWire);
        float nextGreen = channelLevel(greenWire);
        float nextBlue = channelLevel(blueWire);

        if (nextRed == red && nextGreen == green && nextBlue == blue) {
            return;
        }

        red = nextRed;
        green = nextGreen;
        blue = nextBlue;
        syncToClients();
    }

    private static float channelLevel(ElectricWire wire) {
        if (wire == null || wire.getNetwork() == null) {
            return 0.0f;
        }
        float voltage = (float) Math.abs(wire.potentialDifference());
        if (voltage < CHANNEL_ON_THRESHOLD) {
            return 0.0f;
        }
        return Math.min(1.0f, voltage / RATED_CHANNEL_VOLTAGE);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // Доступ к каналам света.

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    /** Горит ли хоть один канал — иначе прибор полностью обесточен и свет не рисуем. */
    public boolean isLit() {
        return red > 0.0f || green > 0.0f || blue > 0.0f;
    }

    /** Цвет каналов RGB в виде {@code 0xRRGGBB}. */
    public int getColor() {
        int r = (int) (red * 255.0f);
        int g = (int) (green * 255.0f);
        int b = (int) (blue * 255.0f);
        return (r << 16) | (g << 8) | b;
    }

    // Клиентские хуки.

    @Override
    public void initialize() {
        super.initialize();
        if (level != null && level.isClientSide && onClientLoad != null) {
            onClientLoad.accept(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        notifyClientRemoved();
        super.onChunkUnloaded();
    }

    @Override
    public void remove() {
        notifyClientRemoved();
        super.remove();
    }

    private void notifyClientRemoved() {
        if (level != null && level.isClientSide && onClientRemove != null) {
            onClientRemove.accept(this);
        }
    }

    // Сохранение. В Create 6 sendData(byte) больше нет: saveAdditional зовёт write(..., false),
    // а writeClient — write(..., true), поэтому один переопределенный write покрывает
    // и диск, и синхронизацию с клиентом.

    @Override
    protected void write(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Red", red);
        tag.putFloat("Green", green);
        tag.putFloat("Blue", blue);
    }

    @Override
    protected void read(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        red = tag.getFloat("Red");
        green = tag.getFloat("Green");
        blue = tag.getFloat("Blue");
        if (clientPacket && level != null && level.isClientSide && onClientChanged != null) {
            onClientChanged.accept(worldPosition, this);
        }
    }
}