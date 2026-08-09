package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.ModBlocks;
import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = AlarmMod.MOD_ID, value = Dist.CLIENT)
public final class AlarmClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("AlarmMod/AlarmClientHandler");
    private static boolean registeredFrameListener;

    private AlarmClientHandler() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        try {
            Class<?> rtlClass = Class.forName("net.minecraft.client.renderer.RenderTypeLookup");
            Method setRenderLayer = rtlClass.getMethod("setRenderLayer", Block.class, RenderType.class);
            setRenderLayer.invoke(null, ModBlocks.ALARM.get(), RenderType.translucent());
        } catch (ReflectiveOperationException e) {
            LOGGER.debug("RenderTypeLookup is unavailable; using the default alarm render layer", e);
        }

        AlarmBlockEntity.onClientLoad = AlarmLightClient::onAlarmLoad;
        AlarmBlockEntity.onClientRemove = AlarmLightClient::onAlarmRemove;
        AlarmBlockEntity.onClientChanged = (pos, be) -> {
            if (be.isAlarmOn()) {
                AlarmLightClient.addOrUpdateLight(be);
            } else {
                AlarmLightClient.onAlarmRemove(pos);
            }
        };

        if (!registeredFrameListener) {
            NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Pre.class, AlarmClientHandler::onRenderFrame);
            registeredFrameListener = true;
        }
    }

    private static void onRenderFrame(RenderFrameEvent.Pre event) {
        AlarmLightClient.tick(event.getPartialTick().getRealtimeDeltaTicks());
    }
}
