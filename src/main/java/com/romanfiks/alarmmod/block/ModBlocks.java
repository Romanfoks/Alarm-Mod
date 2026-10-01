package com.romanfiks.alarmmod.block;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.custom.AcidFluidBlock;
import com.romanfiks.alarmmod.block.custom.RgbAlarmBlock;
import com.romanfiks.alarmmod.fluid.ModFluids;
import com.romanfiks.alarmmod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks { public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AlarmMod.MOD_ID);

    public static final DeferredBlock<Block> REDSTONE_CHARGED_IRON_BLOCK = registerBlock("redstone_charged_iron_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final DeferredBlock<Block> ALARM = registerBlock("alarm",
            () -> new RgbAlarmBlock(BlockBehaviour.Properties.of().noOcclusion().sound(SoundType.METAL)));
    // В ModBlocks.java измени регистрацию ALARM_FLUID_BLOCK:

    // В ModBlocks.java
    public static final DeferredHolder<Block, Block> LAZURITE_ACID_FLUID_BLOCK = BLOCKS.register("lazurite_acid_fluid_block",
            () -> new AcidFluidBlock(
                    ModFluids.LAZURITE_ACID_FLUID.get(), // Добавили .get(), чтобы передать саму жидкость
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                            .noCollission()
                            .noLootTable()
            ));
    public static final DeferredHolder<Block, Block> REDSTONE_ACID_FLUID_BLOCK = BLOCKS.register("redstone_acid_fluid_block",
            () -> new AcidFluidBlock(ModFluids.REDSTONE_ACID_FLUID.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noCollission().noLootTable()));

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockitem(name, toReturn);
        return toReturn;
    }
    public static <T extends Block> void registerBlockitem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }


}
