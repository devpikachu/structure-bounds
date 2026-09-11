package dev.detpikachu.structurebounds.render;

import com.mojang.math.Transformation;
import dev.detpikachu.structurebounds.config.Options;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class BoxDisplays {

    private static final int EDGES_PER_BOX = 12;

    private static final int BARS_PER_AXIS = EDGES_PER_BOX / 3;

    private static final float THICKNESS = 0.1f;
    private static final float HALF_THICKNESS = THICKNESS / 2f;

    public static List<SpawnedDisplay> build(ServerLevel level, BoxKey key) {
        final var center = center(key.bounds());
        final var viewRange = Options.getInstance().getScanRadiusChunks();
        final var edges = new ArrayList<SpawnedDisplay>(EDGES_PER_BOX);

        for (final var transform : edgeTransforms(key.bounds())) {
            edges.add(edge(level, key, viewRange, center, transform));
        }

        return edges;
    }

    private static Vec3 center(BoundingBox bounds) {
        return new Vec3(
                bounds.minX() + bounds.getXSpan() / 2.0,
                bounds.minY() + bounds.getYSpan() / 2.0,
                bounds.minZ() + bounds.getZSpan() / 2.0);
    }

    private static List<Transformation> edgeTransforms(BoundingBox bounds) {
        final var span = new Span(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan());
        final var transforms = new ArrayList<Transformation>(EDGES_PER_BOX);

        transforms.addAll(xBars(span));
        transforms.addAll(yBars(span));
        transforms.addAll(zBars(span));

        return transforms;
    }

    private static List<Transformation> xBars(Span span) {
        final var bars = new ArrayList<Transformation>(BARS_PER_AXIS);

        for (var signY = -1; signY <= 1; signY += 2) {
            for (var signZ = -1; signZ <= 1; signZ += 2) {
                bars.add(barTransform(
                        new Vector3f(
                                -span.halfX() - HALF_THICKNESS,
                                signY * span.halfY() - HALF_THICKNESS,
                                signZ * span.halfZ() - HALF_THICKNESS),
                        new Vector3f(span.x() + THICKNESS, THICKNESS, THICKNESS)));
            }
        }

        return bars;
    }

    private static List<Transformation> yBars(Span span) {
        final var bars = new ArrayList<Transformation>(BARS_PER_AXIS);

        for (var signX = -1; signX <= 1; signX += 2) {
            for (var signZ = -1; signZ <= 1; signZ += 2) {
                bars.add(barTransform(
                        new Vector3f(
                                signX * span.halfX() - HALF_THICKNESS,
                                -span.halfY() + HALF_THICKNESS,
                                signZ * span.halfZ() - HALF_THICKNESS),
                        new Vector3f(THICKNESS, span.y() - THICKNESS, THICKNESS)));
            }
        }

        return bars;
    }

    private static List<Transformation> zBars(Span span) {
        final var bars = new ArrayList<Transformation>(BARS_PER_AXIS);

        for (var signX = -1; signX <= 1; signX += 2) {
            for (var signY = -1; signY <= 1; signY += 2) {
                bars.add(barTransform(
                        new Vector3f(
                                signX * span.halfX() - HALF_THICKNESS,
                                signY * span.halfY() - HALF_THICKNESS,
                                -span.halfZ() + HALF_THICKNESS),
                        new Vector3f(THICKNESS, THICKNESS, span.z() - THICKNESS)));
            }
        }

        return bars;
    }

    private static Transformation barTransform(Vector3f offset, Vector3f scale) {
        return new Transformation(offset, null, scale, null);
    }

    private static SpawnedDisplay edge(
            ServerLevel level, BoxKey key, float viewRange, Vec3 center, Transformation transform) {
        // #if MC_26_2
        // $$ final var display = new Display.BlockDisplay(
        // $$         net.minecraft.world.entity.EntityTypes.BLOCK_DISPLAY, level);
        // #else
        final var display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        // #endif

        display.setPos(center.x, center.y, center.z);
        display.setBlockState(key.color().getBlockState());
        display.setTransformation(transform);
        display.setBrightnessOverride(Brightness.FULL_BRIGHT);
        display.setViewRange(viewRange);
        display.setGlowingTag(key.isGlowing());

        return SpawnedDisplay.of(display);
    }

    private record Span(float x, float y, float z) {

        private float halfX() {
            return this.x / 2f;
        }

        private float halfY() {
            return this.y / 2f;
        }

        private float halfZ() {
            return this.z / 2f;
        }
    }
}
