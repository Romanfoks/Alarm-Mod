package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.AlarmMod;
import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
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
            AlarmLightClient.tick();
        }
    }
}
