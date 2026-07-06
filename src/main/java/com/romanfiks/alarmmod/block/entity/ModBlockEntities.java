package com.romanfiks.alarmmod.block.entity;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AlarmMod.MOD_ID);

    public static final Supplier<BlockEntityType<AlarmBlockEntity>> ALARM_BE =
            BLOCK_ENTITIES.register("alarm_be", () -> BlockEntityType.Builder.of(
                    AlarmBlockEntity::new, ModBlocks.ALARM.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
