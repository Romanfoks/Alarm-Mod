package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.fluid.ModFluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

public class ClientModEvents {

    public static void onClientExtensions(RegisterClientExtensionsEvent event) {

        // 1. ЛАЗУРИТОВАЯ КИСЛОТА
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(AlarmMod.MOD_ID, "block/placeholder_fluid_still");
            private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(AlarmMod.MOD_ID, "block/placeholder_fluid_flow");

            @Override
            public @NotNull ResourceLocation getStillTexture() { return STILL; }
            @Override
            public @NotNull ResourceLocation getFlowingTexture() { return FLOW; }

            @Override
            public int getTintColor() {
                // Изменили FF на 88 (50% прозрачности)
                return 0x326AAD;
            }
        }, ModFluids.LAZURITE_ACID_FLUID_TYPE.get());

        // 2. ТОКСИЧНАЯ ЖИЖА
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(AlarmMod.MOD_ID, "block/placeholder_fluid_still");
            private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(AlarmMod.MOD_ID, "block/placeholder_fluid_flow");

            @Override
            public @NotNull ResourceLocation getStillTexture() { return STILL; }
            @Override
            public @NotNull ResourceLocation getFlowingTexture() { return FLOW; }

            @Override
            public int getTintColor() {
                // Изменили FF на 88 (50% прозрачности)
                return 0x822C2C;
            }
        }, ModFluids.REDSTONE_ACID_TYPE.get());
    }
}