package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.config.Options;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.scan.ScannedStructure;
import dev.detpikachu.structurebounds.scan.StructureScanner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class BoundsSession {

    private final DrawnDisplays drawn = new DrawnDisplays();

    private @Nullable ScannedStructure held;
    private @Nullable ResourceKey<Level> lastWorld;
    private @Nullable PlayerSettings lastSettings;
    private long lastChunk = ChunkPos.INVALID_CHUNK_POS;
    private boolean wasTruncated;
    private boolean isRefreshScheduled;

    public void refresh(Player player, PlayerSettings settings) {
        final var handle = ((CraftPlayer) player).getHandle();
        final var world = handle.level().dimension();
        final var chunk = chunkKey(handle.getBlockX(), handle.getBlockZ());

        if (!world.equals(this.lastWorld)) {
            logDebug("Bounds for {} rebuilding from scratch in {}.", handle.getScoreboardName(), world.identifier());
            this.reset();
        } else if (chunk == this.lastChunk && settings.equals(this.lastSettings)) {
            return;
        }

        this.lastWorld = world;
        this.lastChunk = chunk;
        this.lastSettings = settings;

        logDebug(
                "Bounds pass for {} at chunk {}, threshold {}, all boxes {}, labels {}.",
                handle.getScoreboardName(),
                handle.chunkPosition(),
                settings.boxThreshold(),
                settings.showAllBoxes(),
                settings.showLabels());

        this.draw(player, handle, settings);
    }

    public IsolateOutcome isolate(Player player) {
        if (this.held != null) {
            this.held = null;
            this.lastChunk = ChunkPos.INVALID_CHUNK_POS;

            return IsolateOutcome.RELEASED;
        }

        final var handle = ((CraftPlayer) player).getHandle();
        final var containing = StructureScanner.scan(handle).stream()
                .filter(structure -> structure.bounds().isInside(handle.blockPosition()))
                .toList();

        if (containing.isEmpty()) {
            return IsolateOutcome.NOT_INSIDE;
        }

        this.held = containing.getFirst();
        this.lastChunk = ChunkPos.INVALID_CHUNK_POS;

        return containing.size() > 1 ? IsolateOutcome.HELD_NEAREST : IsolateOutcome.HELD;
    }

    public boolean claimRefresh(Location destination) {
        final var chunk = chunkKey(destination.getBlockX(), destination.getBlockZ());

        if (this.isRefreshScheduled || chunk == this.lastChunk) {
            return false;
        }

        this.isRefreshScheduled = true;

        return true;
    }

    public void releaseRefresh() {
        this.isRefreshScheduled = false;
    }

    public void hide(Player player) {
        this.drawn.hide(((CraftPlayer) player).getHandle());
        this.reset();
    }

    public void reset() {
        this.drawn.reset();
        this.held = null;
        this.lastWorld = null;
        this.lastSettings = null;
        this.lastChunk = ChunkPos.INVALID_CHUNK_POS;
        this.wasTruncated = false;
    }

    private static long chunkKey(int blockX, int blockZ) {
        return ChunkPos.asLong(blockX >> 4, blockZ >> 4);
    }

    private void draw(Player player, ServerPlayer handle, PlayerSettings settings) {
        final var budget = Options.getInstance().getMaxBoxesPerPlayer();
        final var selection =
                BoxSelector.select(this.structures(handle), settings, budget, this.held != null, handle.position());

        logDebug(
                "Bounds for {}: {} box(es) wanted, {} drawn of a {} budget, {} label(s).",
                handle.getScoreboardName(),
                selection.wantedCount(),
                selection.boxes().size(),
                budget,
                selection.labels().size());

        this.drawn.refresh(handle, selection.displays());

        if (selection.isTruncated() && !this.wasTruncated) {
            player.sendMessage(text("Too many structure bounds nearby. Showing the closest ones only.", GRAY));
        }

        this.wasTruncated = selection.isTruncated();
    }

    private List<ScannedStructure> structures(ServerPlayer handle) {
        final var held = this.held;

        if (held == null) {
            return StructureScanner.scan(handle);
        }

        return List.of(StructureScanner.sortPieces(held, handle.position()));
    }
}
