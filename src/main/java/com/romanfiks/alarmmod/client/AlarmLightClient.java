package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.block.entity.AlarmBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("resource")
public class AlarmLightClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("AlarmMod/AlarmLightClient");

    private record AlarmLights(LightRenderHandle<AreaLightData> first, LightRenderHandle<AreaLightData> second, net.minecraft.world.level.Level level) {}

    private static final Map<BlockPos, AlarmLights> LIGHT_MAP = new HashMap<>();

    // Конфигурация
    private static final float LIGHT_DISTANCE = 8.0f;           // Дальность света
    private static final float MAX_LIGHT_BRIGHTNESS = 2.0f;     // Максимальная яркость
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
        LOGGER.info("onAlarmLoad called for {}, isAlarmOn={}", be.getBlockPos(), be.isAlarmOn());
        if (be.isAlarmOn()) {
            addOrUpdateLight(be);
        }
    }

    static void onAlarmRemove(BlockPos pos) {
        LOGGER.info("onAlarmRemove called for {}", pos);
        AlarmLights pair = LIGHT_MAP.remove(pos);
        if (pair != null) {
            if (pair.first() != null) pair.first().free();
            if (pair.second() != null) pair.second().free();
        }
    }

    static void addOrUpdateLight(AlarmBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        LOGGER.info("=== addOrUpdateLight called for {}", pos);

        if (LIGHT_MAP.containsKey(pos)) {
            LOGGER.info("Light already exists for {}", pos);
            return;
        }

        net.minecraft.world.level.Level level = be.getLevel();
        if (level == null) {
            LOGGER.warn("Cannot add light for {} - level is null", pos);
            return;
        }

        // Get world position (handles Sable sub-levels correctly)
        Vector3f worldPos = getWorldPositionWithLevel(level, pos);
        LOGGER.info("World position for {}: ({}, {}, {})", pos, worldPos.x, worldPos.y, worldPos.z);
        
        float cx = worldPos.x;
        float cy = worldPos.y;
        float cz = worldPos.z;

        // Calculate brightness from redstone signal (0-15 -> 0.0-2.0)
        float brightness = (be.getRedstoneSignal() / 15f) * MAX_LIGHT_BRIGHTNESS;
        LOGGER.info("Redstone signal: {}, calculated brightness: {}", be.getRedstoneSignal(), brightness);

        AreaLightData light1 = createLightData(brightness);
        light1.getPositionMutable().set(cx, cy, cz);
        LOGGER.info("Light1 position set to ({}, {}, {})", cx, cy, cz);


        AreaLightData light2 = createLightData(brightness);
        light2.getPositionMutable().set(cx, cy, cz);
        LOGGER.info("Light2 position set to ({}, {}, {})", cx, cy, cz);

        try {
            LOGGER.info("Adding lights to Veil renderer...");
            LightRenderHandle<AreaLightData> h1 = VeilRenderSystem.renderer().getLightRenderer().addLight(light1);
            LightRenderHandle<AreaLightData> h2 = VeilRenderSystem.renderer().getLightRenderer().addLight(light2);

            LIGHT_MAP.put(pos, new AlarmLights(h1, h2, level));
            LOGGER.info("=== Light added successfully for {} at position ({}, {}, {})", pos, cx, cy, cz);
        } catch (Exception e) {
            LOGGER.error("Failed to add light for {}", pos, e);
        }
    }

    private static AreaLightData createLightData(float brightness) {
        AreaLightData light = new AreaLightData();
        light.setOcclusionEnabled(false);
        light.setSize(1.0f, 1.0f);
        light.setAngle((float) Math.toRadians(120));
        light.setDistance(LIGHT_DISTANCE);
        light.setColor(1, 0, 0);
        light.setBrightness(brightness);
        LOGGER.info("Created light: brightness={}, distance={}, color=red", brightness, LIGHT_DISTANCE);
        return light;
    }

    /**
     * Get world position from AlarmBlockEntity, handling Sable sub-levels
     */

    private static void updatePositions(BlockPos pos, AlarmLights pair, long gameTicks) {
        // Get world position with access to level for proper Sable transformation
        net.minecraft.world.level.Level level = pair.level();
        if (level == null) {
            LOGGER.warn("Cannot update light position for {} - level is null", pos);
            return;
        }
        
        Vector3f worldPos = getWorldPositionWithLevel(level, pos);
        
        float cx = worldPos.x;
        float cy = worldPos.y;
        float cz = worldPos.z;

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

    /**
     * Get world position with access to level for proper Sable transformation
     */
    private static Vector3f getWorldPositionWithLevel(net.minecraft.world.level.Level level, BlockPos pos) {
        try {
            // Check if level is a sub-level by trying to access Sable Companion
            // Use Sable Companion to transform position
            Class<?> sableCompanionClass = Class.forName("dev.ryanhcode.sablecompanion.SableCompanion");
            Object companionInstance = sableCompanionClass.getField("INSTANCE").get(null);
            
            // Get the projection method
            java.lang.reflect.Method projectOutMethod = sableCompanionClass.getMethod(
                "projectOutOfSubLevel", 
                net.minecraft.world.level.Level.class, 
                net.minecraft.world.phys.Vec3.class
            );
            
            // Create Vec3 from block position
            net.minecraft.world.phys.Vec3 blockVec = new net.minecraft.world.phys.Vec3(
                pos.getX() + 0.5, 
                pos.getY() + 0.5, 
                pos.getZ() + 0.5
            );
            
            // Project out of sub-level
            net.minecraft.world.phys.Vec3 projectedVec = (net.minecraft.world.phys.Vec3) projectOutMethod.invoke(
                companionInstance, 
                level, 
                blockVec
            );
            
            LOGGER.debug("Transformed position for {}: {} -> {}", pos, blockVec, projectedVec);
            return new Vector3f((float) projectedVec.x, (float) projectedVec.y, (float) projectedVec.z);
        } catch (ClassNotFoundException e) {
            // Sable Companion not available - this is normal when not in a sub-level
            LOGGER.debug("Sable Companion not available, using regular coordinates");
            return new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        } catch (Exception e) {
            // Other reflection errors
            LOGGER.debug("Sable transformation failed, using default position: {}", e.getMessage());
            return new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        }
    }
}