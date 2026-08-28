package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.StructureBounds;
import dev.detpikachu.structurebounds.config.Options;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;

@ApiStatus.Internal
public final class BoundsManager {

    private static final long PERIOD_TICKS = 10L;

    private static final Map<UUID, BoundsSession> SESSIONS = new HashMap<>();

    public static void start(StructureBounds plugin) {
        plugin.getServer()
                .getGlobalRegionScheduler()
                .runAtFixedRate(plugin, task -> tick(plugin), PERIOD_TICKS, PERIOD_TICKS);

        logDebug(
                "Bounds task started every {} tick(s), scan radius {} chunk(s), budget {} box(es) per player.",
                PERIOD_TICKS,
                Options.getInstance().getScanRadiusChunks(),
                Options.getInstance().getMaxBoxesPerPlayer());
    }

    public static void stop(StructureBounds plugin) {
        plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);

        logDebug("Bounds task stopped, clearing {} session(s).", SESSIONS.size());

        for (final var player : plugin.getServer().getOnlinePlayers()) {
            final var session = SESSIONS.get(player.getUniqueId());

            if (session != null) {
                session.clear(player);
            }
        }

        SESSIONS.clear();
    }

    private static void tick(StructureBounds plugin) {
        final var server = plugin.getServer();
        final var enabled = new HashSet<UUID>();

        for (final var player : server.getOnlinePlayers()) {
            final var settings = PlayerSettings.load(player);

            if (!settings.isEnabled()) {
                continue;
            }

            enabled.add(player.getUniqueId());
            SESSIONS.computeIfAbsent(player.getUniqueId(), uuid -> new BoundsSession())
                    .refresh(player, settings);
        }

        SESSIONS.entrySet().removeIf(entry -> {
            if (enabled.contains(entry.getKey())) {
                return false;
            }

            final var player = server.getPlayer(entry.getKey());

            if (player != null) {
                entry.getValue().clear(player);
            }

            logDebug("Bounds session dropped for {}.", entry.getKey());

            return true;
        });
    }
}
