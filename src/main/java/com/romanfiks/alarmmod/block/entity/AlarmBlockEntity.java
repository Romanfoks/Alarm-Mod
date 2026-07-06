package com.romanfiks.alarmmod.block.entity;

import com.romanfiks.alarmmod.block.custom.AlarmBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AlarmBlockEntity extends BlockEntity {

    // Client-side callbacks (set by AlarmClientHandler)
    public static Consumer<AlarmBlockEntity> onClientLoad;
    public static Consumer<BlockPos> onClientRemove;
    public static BiConsumer<BlockPos, AlarmBlockEntity> onClientChanged;

    private boolean isAlarmOn = false;
    private int color = 0xFF0000;

    public AlarmBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALARM_BE.get(), pos, state);
    }

    public boolean isAlarmOn() { return isAlarmOn; }
    public int getColor() { return color; }

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
            onClientRemove.accept(worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("AlarmOn", isAlarmOn);
        tag.putInt("Color", color);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isAlarmOn = tag.getBoolean("AlarmOn");
        this.color = tag.getInt("Color");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            loadAdditional(pkt.getTag(), registries);
            if (level != null && level.isClientSide && onClientChanged != null) {
                onClientChanged.accept(worldPosition, this);
            }
        }
    }
}