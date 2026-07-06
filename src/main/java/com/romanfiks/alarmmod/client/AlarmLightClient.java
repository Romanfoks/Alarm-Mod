package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class AlarmLightClient {

    private record AlarmLights(LightRenderHandle<AreaLightData> first, LightRenderHandle<AreaLightData> second) {}

    private static final Map<BlockPos, AlarmLights> LIGHT_MAP = new HashMap<>();

    static void tick() {
        if (LIGHT_MAP.isEmpty()) return;
        double angle = System.currentTimeMillis() / 3000.0 * Math.PI;
        for (Map.Entry<BlockPos, AlarmLights> entry : LIGHT_MAP.entrySet()) {
            updatePositions(entry.getKey(), entry.getValue(), angle);
        }
    }

    static void onAlarmLoad(AlarmBlockEntity be) {
        if (be.isAlarmOn()) {
            addOrUpdateLight(be);
        }
    }

    static void onAlarmRemove(BlockPos pos) {
        AlarmLights pair = LIGHT_MAP.remove(pos);
        if (pair != null) {
            pair.first().free();
            pair.second().free();
        }
    }

    static void addOrUpdateLight(AlarmBlockEntity be) {
        BlockPos pos = be.getBlockPos();

        AlarmLights pair = LIGHT_MAP.get(pos);
        if (pair == null) {
            float cx = pos.getX() + 0.5f;
            float cy = pos.getY() + 0.5f;
            float cz = pos.getZ() + 0.5f;

            AreaLightData light1 = new AreaLightData();
            light1.setOcclusionEnabled(true);
            light1.setSize(0.2f, 0.2f);
            light1.setAngle((float) Math.toRadians(90));
            light1.setDistance(8.0f);
            light1.setColor(1, 0, 0);
            light1.setBrightness(2.0f);
            light1.getPositionMutable().set(cx, cy, cz);

            AreaLightData light2 = new AreaLightData();
            light2.setOcclusionEnabled(true);
            light2.setSize(0.2f, 0.2f);
            light2.setAngle((float) Math.toRadians(90));
            light2.setDistance(8.0f);
            light2.setColor(1, 0, 0);
            light2.setBrightness(2.0f);
            light2.getPositionMutable().set(cx, cy, cz);

            LightRenderHandle<AreaLightData> h1 = VeilRenderSystem.renderer().getLightRenderer().addLight(light1);
            LightRenderHandle<AreaLightData> h2 = VeilRenderSystem.renderer().getLightRenderer().addLight(light2);
            LIGHT_MAP.put(pos, new AlarmLights(h1, h2));
        }
    }

    private static void updatePositions(BlockPos pos, AlarmLights pair, double angle) {
        float cx = pos.getX() + 0.5f;
        float cy = pos.getY() + 0.6f;
        float cz = pos.getZ() + 0.5f;
        float radius = 0.4f;

        float dx1 = (float) Math.cos(angle);
        float dz1 = (float) Math.sin(angle);
        pair.first().getLightData().getPositionMutable().set(cx + dx1 * radius, cy, cz + dz1 * radius);
        pair.first().getLightData().getOrientationMutable().lookAlong(new Vector3f(dx1, -0.3f, dz1), new Vector3f(0, 1, 0));
        pair.first().markDirty();

        double angle2 = angle + Math.PI;
        float dx2 = (float) Math.cos(angle2);
        float dz2 = (float) Math.sin(angle2);
        pair.second().getLightData().getPositionMutable().set(cx + dx2 * radius, cy, cz + dz2 * radius);
        pair.second().getLightData().getOrientationMutable().lookAlong(new Vector3f(dx2, -0.3f, dz2), new Vector3f(0, 1, 0));
        pair.second().markDirty();
    }
}
