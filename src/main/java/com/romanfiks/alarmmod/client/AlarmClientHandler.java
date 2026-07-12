package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.ModBlocks;
import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import java.lang.reflect.Method;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = AlarmMod.MOD_ID, value = Dist.CLIENT)
public class AlarmClientHandler {

    private static boolean registeredTick;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Устанавливаем рендер-слой для блока Alarm, чтобы полупрозрачные части модели отображались корректно
        try {
            // Используем рефлексию, чтобы избежать жёсткой ссылки на RenderTypeLookup, которая может отсутствовать в некоторых окружениях
            Class<?> rtlClass = Class.forName("net.minecraft.client.renderer.RenderTypeLookup");
            Method setRenderLayer = rtlClass.getMethod("setRenderLayer", Block.class, RenderType.class);
            setRenderLayer.invoke(null, ModBlocks.ALARM.get(), RenderType.translucent());
        } catch (Throwable t) {
            // В некоторых dev-окружениях класс/метод может быть недоступен; безопасно игнорируем ошибку
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
        if (!registeredTick) {
            NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, AlarmClientHandler::onLevelTick);
            registeredTick = true;
        }
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ClientLevel) {
            // Передаём текущее игровое время (тик) в клиентский обработчик света
            AlarmLightClient.tick(event.getLevel().getGameTime());
        }
    }
}
