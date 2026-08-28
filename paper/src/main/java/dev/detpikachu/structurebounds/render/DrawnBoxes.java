package dev.detpikachu.structurebounds.render;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;

@ApiStatus.Internal
public final class DrawnBoxes {

    private final Map<BoxKey, IntList> shown = new HashMap<>();

    public void refresh(ServerPlayer handle, List<BoxKey> selected) {
        this.removeStale(handle, selected);
        this.spawnMissing(handle, selected);
    }

    public void hide(ServerPlayer handle) {
        if (this.shown.isEmpty()) {
            return;
        }

        final var removed = new IntArrayList();

        for (final var ids : this.shown.values()) {
            removed.addAll(ids);
        }

        this.reset();

        handle.connection.send(new ClientboundRemoveEntitiesPacket(removed));
    }

    public void reset() {
        this.shown.clear();
    }

    private void removeStale(ServerPlayer handle, List<BoxKey> selected) {
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

    private void spawnMissing(ServerPlayer handle, List<BoxKey> selected) {
        var spawned = 0;

        for (final var key : selected) {
            if (this.shown.containsKey(key)) {
                continue;
            }

            final var ids = new IntArrayList(BoxDisplays.EDGES_PER_BOX);

            for (final var edge : BoxDisplays.build(handle.level(), key)) {
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
