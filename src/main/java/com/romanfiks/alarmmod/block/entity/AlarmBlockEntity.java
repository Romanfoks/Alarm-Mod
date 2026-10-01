package com.romanfiks.alarmmod.block.entity;

import com.romanfiks.alarmmod.block.custom.AlarmBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AlarmBlockEntity extends BlockEntity {

    // Client-side callbacks (set by AlarmClientHandler)
    public static Consumer<AlarmBlockEntity> onClientLoad;
    public static Consumer<AlarmBlockEntity> onClientRemove;
    public static BiConsumer<BlockPos, AlarmBlockEntity> onClientChanged;

    private boolean isAlarmOn = false;
    private int color = 0xFF0000;
    private int redstoneSignal = 0;

    public AlarmBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALARM_BE.get(), pos, state);
    }

    public boolean isAlarmOn() { return isAlarmOn; }
    public int getColor() { return color; }
    public int getRedstoneSignal() { return redstoneSignal; }

    public void setAlarmOn(boolean on) {
        this.isAlarmOn = on;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            if (level.isClientSide && onClientChanged != null) {
                onClientChanged.accept(worldPosition, this);
            }
        }
    }

    public void setRedstoneSignal(int signal) {
        int clamped = Math.max(0, Math.min(15, signal));
        if (clamped == this.redstoneSignal) {
            return;
        }
        this.redstoneSignal = clamped;
        this.isAlarmOn = clamped > 0;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            if (level.isClientSide && onClientChanged != null) {
                onClientChanged.accept(worldPosition, this);
            }
        }
    }

    public void setColor(int color) {
        this.color = color;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            if (level.isClientSide && onClientChanged != null) {
                onClientChanged.accept(worldPosition, this);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            if (level.isClientSide && onClientLoad != null) {
                onClientLoad.accept(this);
            } else if (!level.isClientSide) {
                AlarmBlock.updateAlarm(level, worldPosition);
            }
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && level.isClientSide && onClientRemove != null) {
            onClientRemove.accept(this);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        tag.putBoolean("AlarmOn", isAlarmOn);
        tag.putInt("Color", color);
        tag.putInt("RedstoneSignal", redstoneSignal);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isAlarmOn = tag.getBoolean("AlarmOn");
        this.color = tag.getInt("Color");
        this.redstoneSignal = tag.getInt("RedstoneSignal");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, @NotNull HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            loadAdditional(pkt.getTag(), registries);
            if (level != null && level.isClientSide && onClientChanged != null) {
                onClientChanged.accept(worldPosition, this);
            }
        }
    }
}