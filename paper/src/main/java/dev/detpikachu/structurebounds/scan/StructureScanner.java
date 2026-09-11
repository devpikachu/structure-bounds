package dev.detpikachu.structurebounds.scan;

import dev.detpikachu.structurebounds.config.Options;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;

@ApiStatus.Internal
public final class StructureScanner {

    public static Scan scan(ServerPlayer handle) {
        final var level = handle.level();
        final var position = handle.position();
        final var centerChunk = handle.chunkPosition();
        final var radius = Options.getInstance().getScanRadiusChunks();
        final var starts = new LinkedHashSet<StructureStart>();
        final var resolved = new HashMap<Structure, LongSet>();
        var isRadiusComplete = true;

        for (var chunkX = centerChunk.x - radius; chunkX <= centerChunk.x + radius; chunkX++) {
            for (var chunkZ = centerChunk.z - radius; chunkZ <= centerChunk.z + radius; chunkZ++) {
                if (!collectFromChunk(level, chunkX, chunkZ, resolved, starts)) {
                    isRadiusComplete = false;
                }
            }
        }

        final var scanned = starts.stream()
                .map(start -> toScannedStructure(start, position))
                .sorted(Comparator.comparingDouble(structure -> distanceSquared(position, structure.bounds())))
                .toList();

        logDebug(
                "Scan for {} at chunk {} within {} chunk(s) found {} structure(s), radius complete {}.",
                handle.getScoreboardName(),
                centerChunk,
                radius,
                scanned.size(),
                isRadiusComplete);

        return new Scan(scanned, isRadiusComplete);
    }

    public static ScannedStructure sortPieces(ScannedStructure structure, Vec3 position) {
        final var sorted = new ArrayList<>(structure.pieces());

        sorted.sort(Comparator.comparingDouble(piece -> distanceSquared(position, piece.bounds())));

        return new ScannedStructure(structure.bounds(), sorted);
    }

    private static boolean collectFromChunk(
            ServerLevel level, int chunkX, int chunkZ, Map<Structure, LongSet> resolved, Set<StructureStart> starts) {
        final var chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_REFERENCES, false);

        if (chunk == null) {
            return false;
        }

        var isComplete = true;

        for (final var references : chunk.getAllReferences().entrySet()) {
            final var structure = references.getKey();
            final var seen = resolved.computeIfAbsent(structure, key -> new LongOpenHashSet());
            final var positions = references.getValue().iterator();

            while (positions.hasNext()) {
                final var packedChunkPosition = positions.nextLong();

                if (seen.add(packedChunkPosition) && !resolveStart(level, structure, packedChunkPosition, starts)) {
                    isComplete = false;
                }
            }
        }

        return isComplete;
    }

    private static boolean resolveStart(
            ServerLevel level, Structure structure, long packedChunkPosition, Set<StructureStart> starts) {
        final var startChunk = new ChunkPos(packedChunkPosition);
        final var chunk = level.getChunk(startChunk.x, startChunk.z, ChunkStatus.STRUCTURE_STARTS, false);

        if (chunk == null) {
            return false;
        }

        final var start = chunk.getStartForStructure(structure);

        if (start != null && start.isValid()) {
            starts.add(start);
        }

        return true;
    }

    private static ScannedStructure toScannedStructure(StructureStart start, Vec3 position) {
        final var pieces = start.getPieces();
        final var described = new ArrayList<ScannedStructure.Piece>(pieces.size());

        for (var i = 0; i < pieces.size(); i++) {
            final var piece = pieces.get(i);
            described.add(new ScannedStructure.Piece(piece.getBoundingBox(), i == 0, pieceName(piece)));
        }

        return sortPieces(new ScannedStructure(start.getBoundingBox(), described), position);
    }

    private static @Nullable String pieceName(StructurePiece piece) {
        if (piece instanceof PoolElementStructurePiece pool) {
            return pool.getElement() instanceof SinglePoolElement single ? templateName(single) : null;
        }

        final var type = BuiltInRegistries.STRUCTURE_PIECE.getKey(piece.getType());

        return type == null ? null : shortName(type);
    }

    private static @Nullable String templateName(SinglePoolElement element) {
        try {
            return shortName(element.getTemplateLocation());
        } catch (RuntimeException exception) {
            logDebug("Skipped a piece name for a pool element holding an inline template.", exception);
            return null;
        }
    }

    private static String shortName(Identifier identifier) {
        final var path = identifier.getPath();

        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static double distanceSquared(Vec3 position, BoundingBox bounds) {
        final var deltaX = bounds.minX() + bounds.getXSpan() / 2.0 - position.x;
        final var deltaY = bounds.minY() + bounds.getYSpan() / 2.0 - position.y;
        final var deltaZ = bounds.minZ() + bounds.getZSpan() / 2.0 - position.z;

        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }
}
