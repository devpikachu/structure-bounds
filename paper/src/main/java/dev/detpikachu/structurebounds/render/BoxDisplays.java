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

    private static final float THICKNESS = 0.1f;
    private static final float HALF_THICKNESS = THICKNESS / 2f;

    public static List<BoxEdge> build(ServerLevel level, BoundingBox bounds, BoxColor color) {
        final var center = center(bounds);
        final var viewRange = Options.getInstance().getScanRadiusChunks();
        final var edges = new ArrayList<BoxEdge>(EDGES_PER_BOX);

        for (final var transform : edgeTransforms(bounds)) {
            edges.add(edge(level, color, viewRange, center, transform));
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
        final var spanX = bounds.maxX() - bounds.minX() + 1;
        final var spanY = bounds.maxY() - bounds.minY() + 1;
        final var spanZ = bounds.maxZ() - bounds.minZ() + 1;
        final var halfX = spanX / 2.0f;
        final var halfY = spanY / 2.0f;
        final var halfZ = spanZ / 2.0f;
        final var transforms = new ArrayList<Transformation>(EDGES_PER_BOX);

        for (var firstSign = -1; firstSign <= 1; firstSign += 2) {
            for (var secondSign = -1; secondSign <= 1; secondSign += 2) {
                transforms.add(barTransform(
                        -halfX - HALF_THICKNESS,
                        firstSign * halfY - HALF_THICKNESS,
                        secondSign * halfZ - HALF_THICKNESS,
                        spanX + THICKNESS,
                        THICKNESS,
                        THICKNESS));
                transforms.add(barTransform(
                        firstSign * halfX - HALF_THICKNESS,
                        -halfY + HALF_THICKNESS,
                        secondSign * halfZ - HALF_THICKNESS,
                        THICKNESS,
                        spanY - THICKNESS,
                        THICKNESS));
                transforms.add(barTransform(
                        firstSign * halfX - HALF_THICKNESS,
                        secondSign * halfY - HALF_THICKNESS,
                        -halfZ + HALF_THICKNESS,
                        THICKNESS,
                        THICKNESS,
                        spanZ - THICKNESS));
            }
        }

        return transforms;
    }

    private static Transformation barTransform(float x, float y, float z, float scaleX, float scaleY, float scaleZ) {
        return new Transformation(new Vector3f(x, y, z), null, new Vector3f(scaleX, scaleY, scaleZ), null);
    }

    private static BoxEdge edge(
            ServerLevel level, BoxColor color, float viewRange, Vec3 center, Transformation transform) {
        final var display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);

        display.setPos(center.x, center.y, center.z);
        display.setBlockState(color.getBlockState());
        display.setTransformation(transform);
        display.setBrightnessOverride(Brightness.FULL_BRIGHT);
        display.setViewRange(viewRange);

        final var addPacket = new ClientboundAddEntityPacket(
                display.getId(),
                display.getUUID(),
                center.x,
                center.y,
                center.z,
                0.0f,
                0.0f,
                EntityType.BLOCK_DISPLAY,
                0,
                Vec3.ZERO,
                0.0);
        final var dataPacket = new ClientboundSetEntityDataPacket(
                display.getId(), Objects.requireNonNull(display.getEntityData().getNonDefaultValues()));

        return new BoxEdge(display.getId(), addPacket, dataPacket);
    }
}
