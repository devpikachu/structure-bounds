package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.config.Options;
import dev.detpikachu.structurebounds.player.PlayerSettings;
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

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class BoundsSession {

    private final DrawnBoxes drawn = new DrawnBoxes();

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
                "Bounds pass for {} at chunk {}, threshold {}, all boxes {}.",
                handle.getScoreboardName(),
                handle.chunkPosition(),
                settings.boxThreshold(),
                settings.showAllBoxes());

        this.draw(player, handle, settings);
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
        final var selection = BoxSelector.select(StructureScanner.scan(handle), settings, budget);

        logDebug(
                "Bounds for {}: {} box(es) wanted, {} drawn of a {} budget.",
                handle.getScoreboardName(),
                selection.wantedCount(),
                selection.boxes().size(),
                budget);

        this.drawn.refresh(handle, selection.boxes());

        if (selection.isTruncated() && !this.wasTruncated) {
            player.sendMessage(text("Too many structure bounds nearby. Showing the closest ones only.", GRAY));
        }

        this.wasTruncated = selection.isTruncated();
    }
}
