package com.romanfiks.alarmmod.fluid;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.ModBlocks;
import com.romanfiks.alarmmod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, AlarmMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, AlarmMod.MOD_ID);

    // 1. ТИП ЖИДКОСТИ
    public static final DeferredHolder<FluidType, FluidType> LAZURITE_ACID_FLUID_TYPE = FLUID_TYPES.register("lazurite_acid_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.alarmmod.lazurite_acid")
                    .canPushEntity(true)
                    .canSwim(true)
                    .canDrown(true)
                    .density(1500)
                    .viscosity(2000)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));


    // 2. ИСТОЧНИК
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> LAZURITE_ACID_FLUID = FLUIDS.register("lazurite_acid_fluid",
            () -> new BaseFlowingFluid.Source(ModFluids.ALARM_PROPERTIES));

    // 3. ТЕКУЧАЯ
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_LAZURITE_ACID_FLUID = FLUIDS.register("lazurite_acid_fluid_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.ALARM_PROPERTIES));

    // СВОЙСТВА
    public static final BaseFlowingFluid.Properties ALARM_PROPERTIES = new BaseFlowingFluid.Properties(
            LAZURITE_ACID_FLUID_TYPE, LAZURITE_ACID_FLUID, FLOWING_LAZURITE_ACID_FLUID)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (LiquidBlock) ModBlocks.LAZURITE_ACID_FLUID_BLOCK.get())
            .bucket(() -> ModItems.LAZURITE_ACID_FLUID_BUCKET.get());

    // --- ВТОРАЯ ЖИДКОСТЬ (TOXIC) ---

    public static final DeferredHolder<FluidType, FluidType> REDSTONE_ACID_TYPE = FLUID_TYPES.register("redstone_acid_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.alarmmod.redstone_acid")
                    .canPushEntity(true).canSwim(true).canDrown(true)
                    .density(1500).viscosity(2000)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> REDSTONE_ACID_FLUID = FLUIDS.register("redstone_acid_fluid",
            () -> new BaseFlowingFluid.Source(ModFluids.TOXIC_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_REDSTONE_ACID_FLUID = FLUIDS.register("redstone_acid_fluid_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.TOXIC_PROPERTIES));

    public static final BaseFlowingFluid.Properties TOXIC_PROPERTIES = new BaseFlowingFluid.Properties(
            REDSTONE_ACID_TYPE, REDSTONE_ACID_FLUID, FLOWING_REDSTONE_ACID_FLUID)
            .slopeFindDistance(2).levelDecreasePerBlock(2)
            .block(() -> (LiquidBlock) ModBlocks.REDSTONE_ACID_FLUID_BLOCK.get())
            .bucket(() -> ModItems.REDSTONE_ACID_FLUID_BUCKET.get());
}
