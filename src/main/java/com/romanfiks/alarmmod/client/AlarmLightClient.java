package com.romanfiks.alarmmod.client;

import com.romanfiks.alarmmod.block.custom.RgbAlarmBlock;
import com.romanfiks.alarmmod.block.entity.RgbAlarmBlockEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("resource")
public final class AlarmLightClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("AlarmMod/AlarmLightClient");

    private record AlarmLights(
        LightRenderHandle<PointLightData> ambient,
        LightRenderHandle<AreaLightData> firstSweep,
        LightRenderHandle<AreaLightData> secondSweep,
        Level level
    ) {}

    /**
     * Ключ карты света: координаты сами по себе не уникальны, одна и та же сирена
     * может стоять в разных измерениях.
     */
    private record LightKey(ResourceKey<Level> dimension, BlockPos pos) {}

    private static final Map<LightKey, AlarmLights> LIGHT_MAP = new HashMap<>();

    private static final float AMBIENT_RADIUS = 5.5f;
    private static final float AMBIENT_MAX_BRIGHTNESS = 0.45f;
    private static final float SWEEP_DISTANCE = 9.0f;
    private static final float SWEEP_MAX_BRIGHTNESS = 1.35f;
    private static final float SWEEP_ANGLE = (float) Math.toRadians(28.0D);
    private static final long ROTATION_PERIOD_TICKS = 20L;
    private static final double ROTATION_RADIANS_PER_TICK = Math.PI * 2.0D / ROTATION_PERIOD_TICKS;
    private static final double LIGHT_FACE_OFFSET = 0.02D;
    private static double rotationAngle;

    private AlarmLightClient() {
    }

    static void tick(float realtimeDeltaTicks) {
        if (LIGHT_MAP.isEmpty()) {
            return;
        }

        rotationAngle = (rotationAngle + Math.max(0.0f, realtimeDeltaTicks) * ROTATION_RADIANS_PER_TICK)
            % (Math.PI * 2.0D);

        for (Map.Entry<LightKey, AlarmLights> entry : LIGHT_MAP.entrySet()) {
            updateLights(entry.getKey(), entry.getValue(), rotationAngle);
        }
    }

    static void onAlarmLoad(RgbAlarmBlockEntity be) {
        if (be.isLit()) {
            addOrUpdateLight(be);
        }
    }

    static void onAlarmRemove(RgbAlarmBlockEntity be) {
        Level level = be.getLevel();
        if (level == null) {
            LOGGER.warn("Cannot remove alarm light for {} because its level is null", be.getBlockPos());
            return;
        }
        AlarmLights lights = LIGHT_MAP.remove(new LightKey(level.dimension(), be.getBlockPos()));
        if (lights != null) {
            free(lights);
        }
    }

    /**
     * Вызывается при выходе из мира: setRemoved() у сущности вызывается не для всех
     * сирен, поэтому без этого ручного сброса ресурсы света Veil остаются выделенными.
     */
    static void releaseAll() {
        for (AlarmLights lights : LIGHT_MAP.values()) {
            free(lights);
        }
        LIGHT_MAP.clear();
    }

    private static void free(AlarmLights lights) {
        lights.ambient().free();
        lights.firstSweep().free();
        lights.secondSweep().free();
    }

    static void addOrUpdateLight(RgbAlarmBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        Level level = be.getLevel();
        if (level == null) {
            LOGGER.warn("Cannot add alarm light for {} because its level is null", pos);
            return;
        }

        LightKey key = new LightKey(level.dimension(), pos);
        if (LIGHT_MAP.containsKey(key)) {
            return;
        }

        PointLightData ambient = createAmbientLight();
        AreaLightData firstSweep = createSweepLight();
        AreaLightData secondSweep = createSweepLight();
        updateLightData(level, pos, rotationAngle, ambient, firstSweep, secondSweep);
        applyRgb(ambient, firstSweep, secondSweep, be);

        LightRenderHandle<PointLightData> ambientHandle = null;
        LightRenderHandle<AreaLightData> firstSweepHandle = null;
        LightRenderHandle<AreaLightData> secondSweepHandle = null;
        try {
            ambientHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(ambient);
            firstSweepHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(firstSweep);
            secondSweepHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(secondSweep);
            LIGHT_MAP.put(key, new AlarmLights(ambientHandle, firstSweepHandle, secondSweepHandle, level));
        } catch (RuntimeException e) {
            if (ambientHandle != null) {
                ambientHandle.free();
            }
            if (firstSweepHandle != null) {
                firstSweepHandle.free();
            }
            if (secondSweepHandle != null) {
                secondSweepHandle.free();
            }
            LOGGER.error("Failed to add alarm lights for {} in {}", pos, level.dimension().location(), e);
        }
    }

    private static PointLightData createAmbientLight() {
        PointLightData light = new PointLightData();
        light.setOcclusionEnabled(true);
        light.setRadius(AMBIENT_RADIUS);
        light.setBrightness(0.0f);
        return light;
    }

    private static AreaLightData createSweepLight() {
        AreaLightData light = new AreaLightData();
        light.setOcclusionEnabled(true);
        light.setSize(0.16D, 0.16D);
        light.setAngle(SWEEP_ANGLE);
        light.setDistance(SWEEP_DISTANCE);
        light.setBrightness(0.0f);
        return light;
    }

    /**
     * Переносит каналы сирены на свет Veil: цвет берётся из самих каналов, а общая
     * яркость — по самому яркому из них.
     */
    private static void applyRgb(
        PointLightData ambient,
        AreaLightData firstSweep,
        AreaLightData secondSweep,
        RgbAlarmBlockEntity be
    ) {
        float red = be.getRed();
        float green = be.getGreen();
        float blue = be.getBlue();
        float intensity = Math.max(red, Math.max(green, blue));

        ambient.setColor(red, green, blue);
        firstSweep.setColor(red, green, blue);
        secondSweep.setColor(red, green, blue);

        ambient.setBrightness(intensity * AMBIENT_MAX_BRIGHTNESS);
        firstSweep.setBrightness(intensity * SWEEP_MAX_BRIGHTNESS);
        secondSweep.setBrightness(intensity * SWEEP_MAX_BRIGHTNESS);
    }

    private static void updateLights(LightKey key, AlarmLights lights, double angle) {
        Level level = lights.level();
        BlockPos pos = key.pos();

        PointLightData ambient = lights.ambient().getLightData();
        AreaLightData firstSweep = lights.firstSweep().getLightData();
        AreaLightData secondSweep = lights.secondSweep().getLightData();
        updateLightData(level, pos, angle, ambient, firstSweep, secondSweep);

        if (level.getBlockEntity(pos) instanceof RgbAlarmBlockEntity be) {
            applyRgb(ambient, firstSweep, secondSweep, be);
        } else {
            ambient.setBrightness(0.0f);
            firstSweep.setBrightness(0.0f);
            secondSweep.setBrightness(0.0f);
        }

        lights.ambient().markDirty();
        lights.firstSweep().markDirty();
        lights.secondSweep().markDirty();
    }

    private static void updateLightData(
        Level level,
        BlockPos pos,
        double angle,
        PointLightData ambient,
        AreaLightData firstSweep,
        AreaLightData secondSweep
    ) {
        Direction facing = getLightDirection(level, pos);
        Vector3f localAxis = directionVector(facing);
        Vector3d localPosition = getLocalLightPosition(pos, localAxis);
        Vector3d worldPosition = projectOutOfSubLevel(level, pos, new Vector3d(localPosition));

        ambient.getPositionMutable().set(worldPosition);
        firstSweep.getPositionMutable().set(worldPosition);
        secondSweep.getPositionMutable().set(worldPosition);

        updateSweepOrientation(level, pos, localPosition, worldPosition, localAxis, angle, firstSweep);
        updateSweepOrientation(level, pos, localPosition, worldPosition, localAxis, angle + Math.PI, secondSweep);
    }

    private static void updateSweepOrientation(
        Level level,
        BlockPos pos,
        Vector3d localPosition,
        Vector3d worldPosition,
        Vector3f localAxis,
        double angle,
        AreaLightData sweep
    ) {
        Vector3f localDirection = getSweepDirection(localAxis, angle);
        Vector3f worldDirection = projectDirection(level, pos, localPosition, worldPosition, localDirection);

        sweep.getOrientationMutable()
            .rotationTo(worldDirection, new Vector3f(0.0f, 0.0f, 1.0f))
            .normalize();
    }

    private static Vector3d getLocalLightPosition(BlockPos pos, Vector3f faceNormal) {
        return new Vector3d(
            pos.getX() + 0.5D + faceNormal.x * LIGHT_FACE_OFFSET,
            pos.getY() + 0.5D + faceNormal.y * LIGHT_FACE_OFFSET,
            pos.getZ() + 0.5D + faceNormal.z * LIGHT_FACE_OFFSET
        );
    }

    private static Vector3f getSweepDirection(Vector3f axis, double angle) {
        Vector3f tangent = Math.abs(axis.y) > 0.5f
            ? new Vector3f(0.0f, 0.0f, 1.0f)
            : new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f bitangent = new Vector3f(axis).cross(tangent).normalize();

        return tangent.mul((float) Math.cos(angle))
            .add(bitangent.mul((float) Math.sin(angle)))
            .normalize();
    }

    private static Vector3f projectDirection(
        Level level,
        BlockPos pos,
        Vector3d localOrigin,
        Vector3d worldOrigin,
        Vector3f localDirection
    ) {
        Vector3d localEndpoint = new Vector3d(localOrigin).add(localDirection.x, localDirection.y, localDirection.z);
        Vector3d worldEndpoint = projectOutOfSubLevel(level, pos, localEndpoint);
        Vector3d worldDirection = worldEndpoint.sub(worldOrigin, new Vector3d()).normalize();
        return new Vector3f(
            (float) worldDirection.x,
            (float) worldDirection.y,
            (float) worldDirection.z
        );
    }

    private static Vector3d projectOutOfSubLevel(Level level, BlockPos pos, Vector3d position) {
        try {
            SableCompanion.INSTANCE.projectOutOfSubLevel(level, position);
            return position;
        } catch (RuntimeException e) {
            LOGGER.error(
                "Sable transformation failed for {} in {}",
                pos,
                level.dimension().location(),
                e
            );
            throw e;
        }
    }

    private static Direction getLightDirection(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(RgbAlarmBlock.FACING)) {
            return RgbAlarmBlock.lightDirection(state);
        }
        return Direction.UP;
    }

    private static Vector3f directionVector(Direction direction) {
        return new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }
}