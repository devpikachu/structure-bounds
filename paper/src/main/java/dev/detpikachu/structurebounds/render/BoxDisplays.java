package dev.detpikachu.structurebounds.render;

import com.mojang.math.Transformation;
import dev.detpikachu.structurebounds.config.Options;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
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
import java.util.Objects;

@ApiStatus.Internal
public final class BoxDisplays {

    public static final int EDGES_PER_BOX = 12;

    private static final int BARS_PER_AXIS = EDGES_PER_BOX / 3;

    private static final float THICKNESS = 0.1f;
    private static final float HALF_THICKNESS = THICKNESS / 2f;

    private static final float NO_ROTATION = 0.0f;
    private static final int NO_ENTITY_DATA = 0;

    public static List<BoxEdge> build(ServerLevel level, BoxKey key) {
        final var center = center(key.bounds());
        final var viewRange = Options.getInstance().getScanRadiusChunks();
        final var edges = new ArrayList<BoxEdge>(EDGES_PER_BOX);

        for (final var transform : edgeTransforms(key.bounds())) {
            edges.add(edge(level, key, viewRange, center, transform));
        }

        return edges;
    }

    private static Vec3 center(BoundingBox bounds) {
        return new Vec3(
                bounds.minX() + (bounds.maxX() - bounds.minX() + 1) / 2.0,
                bounds.minY() + (bounds.maxY() - bounds.minY() + 1) / 2.0,
                bounds.minZ() + (bounds.maxZ() - bounds.minZ() + 1) / 2.0);
    }

    private static List<Transformation> edgeTransforms(BoundingBox bounds) {
        final var span = new Span(
                bounds.maxX() - bounds.minX() + 1,
                bounds.maxY() - bounds.minY() + 1,
                bounds.maxZ() - bounds.minZ() + 1);
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

    private static BoxEdge edge(ServerLevel level, BoxKey key, float viewRange, Vec3 center, Transformation transform) {
        final var display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);

        display.setPos(center.x, center.y, center.z);
        display.setBlockState(key.color().getBlockState());
        display.setTransformation(transform);
        display.setBrightnessOverride(Brightness.FULL_BRIGHT);
        display.setViewRange(viewRange);
        display.setGlowingTag(key.isGlowing());

        final var dataPacket = new ClientboundSetEntityDataPacket(
                display.getId(), Objects.requireNonNull(display.getEntityData().getNonDefaultValues()));

        return new BoxEdge(display.getId(), addPacket(display, center), dataPacket);
    }

    private static ClientboundAddEntityPacket addPacket(Display.BlockDisplay display, Vec3 center) {
        return new ClientboundAddEntityPacket(
                display.getId(),
                display.getUUID(),
                center.x,
                center.y,
                center.z,
                NO_ROTATION,
                NO_ROTATION,
                EntityType.BLOCK_DISPLAY,
                NO_ENTITY_DATA,
                Vec3.ZERO,
                NO_ROTATION);
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
