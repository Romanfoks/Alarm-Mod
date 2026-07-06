package com.romanfiks.alarmmod.item;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlarmMod.MOD_ID);

    public static final Supplier<CreativeModeTab> ALARM_ITEMS_TAB = CREATIVE_MODE_TAB.register("alarm_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.REDSTONE_CHARGED_IRON.get()))
                    .title(Component.translatable("creativetab.alarmmod.items"))
                            .displayItems((itemDisplayParameters, output) -> {
                                output.accept(ModItems.REDSTONE_CHARGED_IRON);
                                output.accept(ModBlocks.REDSTONE_CHARGED_IRON_BLOCK);
                                output.accept(ModBlocks.ALARM);
                                output.accept(ModBlocks.MAGIC_BLOCK);
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }


}
