package com.romanfiks.alarmmod;

import com.romanfiks.alarmmod.block.ModBlocks;
import com.romanfiks.alarmmod.block.entity.ModBlockEntities;
import com.romanfiks.alarmmod.client.ClientModEvents;
import com.romanfiks.alarmmod.fluid.ModFluids;
import com.romanfiks.alarmmod.item.ModCreativeModeTabs;
import com.romanfiks.alarmmod.item.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AlarmMod.MOD_ID)
public class AlarmMod {
    public static final String MOD_ID = "alarmmod";
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus and pass them in automatically.
    public AlarmMod(IEventBus modEventBus) {
        modEventBus.addListener(ClientModEvents::onClientExtensions);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus); // Не забудь это!
        ModFluids.FLUIDS.register(modEventBus);
        // И это!
        ModBlockEntities.register(modEventBus);
    }
}
