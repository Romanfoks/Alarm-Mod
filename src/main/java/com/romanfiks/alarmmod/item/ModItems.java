package com.romanfiks.alarmmod.item;

import com.romanfiks.alarmmod.AlarmMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static DeferredRegister.Items ITEMS = DeferredRegister.createItems(AlarmMod.MOD_ID);

    public static final DeferredItem<Item> REDSTONE_IRON = ITEMS.register("redstone_iron",() -> new Item(new Item.Properties()));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
