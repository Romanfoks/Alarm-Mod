package com.romanfiks.alarmmod.item;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.fluid.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AlarmMod.MOD_ID);

    public static final DeferredItem<Item> REDSTONE_CHARGED_IRON = ITEMS.register("redstone_charged_iron",() -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> REDSTONE_IRON = ITEMS.register("redstone_iron",() -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LAPIS_DUST = ITEMS.register("lapis_dust",() -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LAZURITE_ACID_FLUID_BUCKET = ITEMS.register("lazurite_acid_fluid_bucket",
            () -> new BucketItem(ModFluids.LAZURITE_ACID_FLUID.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> REDSTONE_ACID_FLUID_BUCKET = ITEMS.register("redstone_acid_fluid_bucket",
            () -> new BucketItem(ModFluids.REDSTONE_ACID_FLUID.get(), new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
