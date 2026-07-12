package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("resource")
public class AlarmLightClient {

    private record AlarmLights(LightRenderHandle<AreaLightData> first, LightRenderHandle<AreaLightData> second) {}

    private static final Map<BlockPos, AlarmLights> LIGHT_MAP = new HashMap<>();

    // Конфигурация
    private static final float LIGHT_DISTANCE = 8.0f;           // Дальность света
    private static final float LIGHT_BRIGHTNESS = 2.0f;         // Яркость
    private static final long ROTATION_PERIOD = 650;           // Период вращения (мс) - полный оборот за 9 секунд


    // Используем игровое время (тиков) для стабильного, детерминированного вращения
    // Один игровой тик = 50 ms
    static void tick(long gameTicks) {
        if (LIGHT_MAP.isEmpty()) return;


        for (Map.Entry<BlockPos, AlarmLights> entry : LIGHT_MAP.entrySet()) {
            updatePositions(entry.getKey(), entry.getValue(), gameTicks);
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
            if (pair.first() != null) pair.first().free();
            if (pair.second() != null) pair.second().free();
        }
    }

    static void addOrUpdateLight(AlarmBlockEntity be) {
        BlockPos pos = be.getBlockPos();

        if (LIGHT_MAP.containsKey(pos)) {
            return;
        }

        float cx = pos.getX() + 0.5f;
        float cy = pos.getY() + 0.5f;
        float cz = pos.getZ() + 0.5f;

        AreaLightData light1 = createLightData();
        light1.getPositionMutable().set(cx, cy, cz);


        AreaLightData light2 = createLightData();
        light2.getPositionMutable().set(cx, cy, cz);

        LightRenderHandle<AreaLightData> h1 = VeilRenderSystem.renderer().getLightRenderer().addLight(light1);
        LightRenderHandle<AreaLightData> h2 = VeilRenderSystem.renderer().getLightRenderer().addLight(light2);

        LIGHT_MAP.put(pos, new AlarmLights(h1, h2));
    }

    private static AreaLightData createLightData() {
        AreaLightData light = new AreaLightData();
        light.setOcclusionEnabled(true);
        light.setSize(0.2f, 0.2f);
        light.setAngle((float) Math.toRadians(90));
        light.setDistance(LIGHT_DISTANCE);
        light.setColor(1, 0, 0); // Красный
        light.setBrightness(LIGHT_BRIGHTNESS);
        return light;
    }

    private static void updatePositions(BlockPos pos, AlarmLights pair, long gameTicks) {
        float cx = pos.getX() + 0.5f;
        float cy = pos.getY() + 0.6f;
        float cz = pos.getZ() + 0.5f;

        // Вычисляем угол на основе игрового времени
        long periodTicks = Math.max(1, ROTATION_PERIOD / 50);
        double normalized = (gameTicks % periodTicks) / (double) periodTicks;
        double angle = normalized * Math.PI * 2.0;

        // Оба света в одной позиции (центр блока)
        pair.first().getLightData().getPositionMutable().set(cx, cy, cz);
        pair.second().getLightData().getPositionMutable().set(cx, cy, cz);

        // Первый свет - фиксированная ориентация вверх-вниз
        Vector3f direction1 = new Vector3f(0f, 1f, 0f).normalize();
        pair.first().getLightData().getOrientationMutable().lookAlong(direction1, new Vector3f(0, 1, 0));
        pair.first().markDirty();

        // Второй свет - вращающаяся ориентация (по горизонтали)
        // Используем quaternion для надёжного вращения вокруг Y оси
        Quaternionf rotation = new Quaternionf();
        rotation.rotationY((float) angle);
        Vector3f baseDirection = new Vector3f(0f, 0f, 1f);
        rotation.transform(baseDirection);
        pair.second().getLightData().getOrientationMutable().set(rotation);
        pair.second().markDirty();
    }
}