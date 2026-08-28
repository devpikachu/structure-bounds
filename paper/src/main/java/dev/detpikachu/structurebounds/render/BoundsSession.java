package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.config.Options;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.scan.ScannedStructure;
import dev.detpikachu.structurebounds.scan.StructureScanner;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class BoundsSession {

    private final Map<BoxKey, IntList> shown = new HashMap<>();

    private @Nullable ResourceKey<Level> lastWorld;
    private @Nullable PlayerSettings lastSettings;
    private long lastChunk = ChunkPos.INVALID_CHUNK_POS;
    private boolean wasTruncated;
    private boolean isRefreshScheduled;

    public void refresh(Player player, PlayerSettings settings) {
        final var handle = ((CraftPlayer) player).getHandle();
        final var world = handle.level().dimension();
        final var chunk = handle.chunkPosition().toLong();

        if (!world.equals(this.lastWorld)) {
            logDebug("Bounds for {} rebuilding from scratch in {}.", handle.getScoreboardName(), world.identifier());
            this.forget();
        } else if (chunk == this.lastChunk && settings.equals(this.lastSettings)) {
            return;
        }

        this.lastWorld = world;
        this.lastChunk = chunk;
        this.lastSettings = settings;

        logDebug(
                "Bounds pass for {} at chunk {}, threshold {}, all boxes {}.",
                handle.getScoreboardName(),
                handle.chunkPosition(),
                settings.boxThreshold(),
                settings.showAllBoxes());

        this.update(player, settings, StructureScanner.scan(player));
    }

    public boolean claimRefresh(Location destination) {
        final var chunk = ChunkPos.asLong(destination.getBlockX() >> 4, destination.getBlockZ() >> 4);

        if (this.isRefreshScheduled || chunk == this.lastChunk) {
            return false;
        }

        this.isRefreshScheduled = true;

        return true;
    }

    public void releaseRefresh() {
        this.isRefreshScheduled = false;
    }

    public void forget() {
        this.shown.clear();
        this.lastWorld = null;
        this.lastChunk = ChunkPos.INVALID_CHUNK_POS;
        this.wasTruncated = false;
    }

    public void clear(Player player) {
        if (this.shown.isEmpty()) {
            return;
        }

        final var removed = new IntArrayList();

        for (final var ids : this.shown.values()) {
            removed.addAll(ids);
        }

        this.forget();

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(removed));
    }

    private static List<BoxKey> boxesFor(List<ScannedStructure> structures, PlayerSettings settings) {
        final var keys = new ArrayList<BoxKey>();

        for (final var structure : structures) {
            keys.add(new BoxKey(structure.bounds(), BoxColor.STRUCTURE));
        }

        for (final var structure : structures) {
            for (final var piece : piecesFor(structure, settings)) {
                keys.add(new BoxKey(piece.bounds(), piece.isStart() ? BoxColor.START : BoxColor.PIECE));
            }
        }

        return keys;
    }

    private static List<ScannedStructure.Piece> piecesFor(ScannedStructure structure, PlayerSettings settings) {
        final var pieces = structure.pieces();

        if (settings.showAllBoxes() || pieces.size() < settings.boxThreshold()) {
            return pieces;
        }

        return pieces.subList(0, settings.boxThreshold());
    }

    private void update(Player player, PlayerSettings settings, List<ScannedStructure> structures) {
        final var wanted = boxesFor(structures, settings);
        final var budget = Options.getInstance().getMaxBoxesPerPlayer();
        final var isTruncated = wanted.size() > budget;
        final var selected = isTruncated ? wanted.subList(0, budget) : wanted;
        final var handle = ((CraftPlayer) player).getHandle();

        logDebug(
                "Bounds for {}: {} structure(s), {} box(es) wanted, {} drawn of a {} budget.",
                handle.getScoreboardName(),
                structures.size(),
                wanted.size(),
                selected.size(),
                budget);

        this.removeGone(handle, selected);
        this.spawnNew(handle, selected);

        if (isTruncated && !this.wasTruncated) {
            player.sendMessage(text("Too many structure bounds nearby. Showing the closest ones only.", GRAY));
        }

        this.wasTruncated = isTruncated;
    }

    private void removeGone(ServerPlayer handle, List<BoxKey> selected) {
        final var keep = new HashSet<>(selected);
        final var removed = new IntArrayList();

        this.shown.entrySet().removeIf(entry -> {
            if (keep.contains(entry.getKey())) {
                return false;
            }

            removed.addAll(entry.getValue());
            return true;
        });

        if (!removed.isEmpty()) {
            handle.connection.send(new ClientboundRemoveEntitiesPacket(removed));
            logDebug("Bounds for {}: removed {} entity(ies).", handle.getScoreboardName(), removed.size());
        }
    }

    private void spawnNew(ServerPlayer handle, List<BoxKey> selected) {
        var spawned = 0;

        for (final var key : selected) {
            if (this.shown.containsKey(key)) {
                continue;
            }

            final var ids = new IntArrayList(BoxDisplays.EDGES_PER_BOX);

            for (final var edge : BoxDisplays.build(handle.level(), key.bounds(), key.color())) {
                handle.connection.send(edge.addPacket());
                handle.connection.send(edge.dataPacket());
                ids.add(edge.entityId());
            }

            this.shown.put(key, ids);
            spawned++;
        }

        if (spawned > 0) {
            logDebug(
                    "Bounds for {}: spawned {} box(es), {} now shown.",
                    handle.getScoreboardName(),
                    spawned,
                    this.shown.size());
        }
    }
}
