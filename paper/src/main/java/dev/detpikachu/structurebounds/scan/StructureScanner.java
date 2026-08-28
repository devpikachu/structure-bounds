package dev.detpikachu.structurebounds.scan;

import dev.detpikachu.structurebounds.config.Options;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;

@ApiStatus.Internal
public final class StructureScanner {

    public static List<ScannedStructure> scan(ServerPlayer handle) {
        final var level = handle.level();
        final var position = handle.position();
        final var centerChunk = handle.chunkPosition();
        final var radius = Options.getInstance().getScanRadiusChunks();
        final var starts = new LinkedHashSet<StructureStart>();

        for (var chunkX = centerChunk.x - radius; chunkX <= centerChunk.x + radius; chunkX++) {
            for (var chunkZ = centerChunk.z - radius; chunkZ <= centerChunk.z + radius; chunkZ++) {
                collectFromChunk(level, chunkX, chunkZ, starts);
            }
        }

        final var scanned = starts.stream()
                .map(start -> toScannedStructure(start, position))
                .sorted(Comparator.comparingDouble(structure -> distanceSquared(position, structure.bounds())))
                .toList();

        logDebug(
                "Scan for {} at chunk {} within {} chunk(s) found {} structure(s).",
                handle.getScoreboardName(),
                centerChunk,
                radius,
                scanned.size());

        return scanned;
    }

    public static ScannedStructure sortPieces(ScannedStructure structure, Vec3 position) {
        final var sorted = new ArrayList<>(structure.pieces());

        sorted.sort(Comparator.comparingDouble(piece -> distanceSquared(position, piece.bounds())));

        return new ScannedStructure(structure.bounds(), sorted);
    }

    private static void collectFromChunk(ServerLevel level, int chunkX, int chunkZ, Set<StructureStart> starts) {
        final var chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_REFERENCES, false);

        if (chunk == null) {
            return;
        }

        for (final var references : chunk.getAllReferences().entrySet()) {
            for (final var packedChunkPos : references.getValue()) {
                resolveStart(level, references.getKey(), packedChunkPos, starts);
            }
        }
    }

    private static void resolveStart(
            ServerLevel level, Structure structure, long packedChunkPos, Set<StructureStart> starts) {
        final var chunkPos = new ChunkPos(packedChunkPos);
        final var chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS, false);

        if (chunk == null) {
            return;
        }

        final var start = chunk.getStartForStructure(structure);

        if (start != null && start.isValid()) {
            starts.add(start);
        }
    }

    private static ScannedStructure toScannedStructure(StructureStart start, Vec3 position) {
        final var pieces = start.getPieces();
        final var described = new ArrayList<ScannedStructure.Piece>(pieces.size());

        for (var i = 0; i < pieces.size(); i++) {
            described.add(new ScannedStructure.Piece(pieces.get(i).getBoundingBox(), i == 0));
        }

        return sortPieces(new ScannedStructure(start.getBoundingBox(), described), position);
    }

    private static double distanceSquared(Vec3 position, BoundingBox bounds) {
        final var center = bounds.getCenter();
        final var deltaX = center.getX() - position.x;
        final var deltaY = center.getY() - position.y;
        final var deltaZ = center.getZ() - position.z;

        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }
}
