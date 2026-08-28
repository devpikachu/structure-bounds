package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.StructureBounds;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;

@ApiStatus.Internal
public final class BoundsManager {

    private static final Map<UUID, BoundsSession> SESSIONS = new HashMap<>();

    public static void stop() {
        final var plugin = StructureBounds.getInstance();

        plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);

        logDebug("Bounds stopping, clearing {} session(s).", SESSIONS.size());

        for (final var player : plugin.getServer().getOnlinePlayers()) {
            final var session = SESSIONS.get(player.getUniqueId());

            if (session != null) {
                session.hide(player);
            }
        }

        SESSIONS.clear();
    }

    public static void apply(Player player, PlayerSettings settings) {
        settings.save(player);
        reconcile(player);
    }

    public static IsolateOutcome isolate(Player player) {
        final var outcome = SESSIONS.computeIfAbsent(player.getUniqueId(), uuid -> new BoundsSession())
                .isolate(player);

        reconcile(player);

        return outcome;
    }

    public static void reconcile(Player player) {
        final var settings = PlayerSettings.load(player);

        if (!settings.isEnabled()) {
            hide(player);
            return;
        }

        SESSIONS.computeIfAbsent(player.getUniqueId(), uuid -> new BoundsSession())
                .refresh(player, settings);
    }

    public static void schedule(Player player, Location destination) {
        final var session = SESSIONS.get(player.getUniqueId());

        if (session == null || !session.claimRefresh(destination)) {
            return;
        }

        final var plugin = StructureBounds.getInstance();
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> runScheduledRefresh(player));
    }

    public static void drop(Player player) {
        if (SESSIONS.remove(player.getUniqueId()) != null) {
            logDebug("Bounds session dropped for {} on quit.", player.getName());
        }
    }

    private static void hide(Player player) {
        final var session = SESSIONS.remove(player.getUniqueId());

        if (session == null) {
            return;
        }

        session.hide(player);
        logDebug("Bounds hidden for {}.", player.getName());
    }

    private static void runScheduledRefresh(Player player) {
        final var session = SESSIONS.get(player.getUniqueId());

        if (session == null) {
            return;
        }

        session.releaseRefresh();

        if (player.isOnline()) {
            reconcile(player);
        }
    }
}
