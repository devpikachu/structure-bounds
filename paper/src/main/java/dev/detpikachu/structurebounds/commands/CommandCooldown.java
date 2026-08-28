package dev.detpikachu.structurebounds.commands;

import dev.detpikachu.structurebounds.StructureBounds;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public final class CommandCooldown {

    private static final Map<UUID, Integer> LAST_COMMAND_TICKS = new HashMap<>();

    private static final int COOLDOWN_TICKS = 40;

    public static boolean claim(Player player) {
        final var tick = StructureBounds.getInstance().getServer().getCurrentTick();
        final var last = LAST_COMMAND_TICKS.get(player.getUniqueId());

        if (last != null && tick - last < COOLDOWN_TICKS) {
            return false;
        }

        LAST_COMMAND_TICKS.put(player.getUniqueId(), tick);

        return true;
    }

    public static void drop(Player player) {
        LAST_COMMAND_TICKS.remove(player.getUniqueId());
    }

    public static void dropAll() {
        LAST_COMMAND_TICKS.clear();
    }
}
