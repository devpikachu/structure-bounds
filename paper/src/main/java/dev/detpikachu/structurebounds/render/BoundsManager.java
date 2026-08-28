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
    private static final Map<UUID, Integer> LAST_COMMAND_TICKS = new HashMap<>();

    private static final int COMMAND_COOLDOWN_TICKS = 40;

    public static void stop() {
        final var plugin = StructureBounds.getInstance();

        plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);

        logDebug("Bounds stopping, clearing {} session(s).", SESSIONS.size());

        for (final var player : plugin.getServer().getOnlinePlayers()) {
            final var session = SESSIONS.get(player.getUniqueId());

            if (session != null) {
                session.clear(player);
            }
        }

        SESSIONS.clear();
        LAST_COMMAND_TICKS.clear();
    }

    public static void refresh(Player player) {
        final var settings = PlayerSettings.load(player);

        if (!settings.isEnabled()) {
            clearSession(player);
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
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> refreshScheduled(player));
    }

    public static boolean claimCommandRefresh(Player player) {
        final var tick = StructureBounds.getInstance().getServer().getCurrentTick();
        final var last = LAST_COMMAND_TICKS.get(player.getUniqueId());

        if (last != null && tick - last < COMMAND_COOLDOWN_TICKS) {
            return false;
        }

        LAST_COMMAND_TICKS.put(player.getUniqueId(), tick);

        return true;
    }

    public static void drop(Player player) {
        LAST_COMMAND_TICKS.remove(player.getUniqueId());

        if (SESSIONS.remove(player.getUniqueId()) != null) {
            logDebug("Bounds session dropped for {} on quit.", player.getName());
        }
    }

    private static void clearSession(Player player) {
        final var session = SESSIONS.remove(player.getUniqueId());

        if (session == null) {
            return;
        }

        session.clear(player);
        logDebug("Bounds session cleared for {}.", player.getName());
    }

    private static void refreshScheduled(Player player) {
        final var session = SESSIONS.get(player.getUniqueId());

        if (session == null) {
            return;
        }

        session.releaseRefresh();

        if (player.isOnline()) {
            refresh(player);
        }
    }
}
