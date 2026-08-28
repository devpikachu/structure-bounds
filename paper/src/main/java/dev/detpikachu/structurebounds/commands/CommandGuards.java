package dev.detpikachu.structurebounds.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.render.BoundsManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.StructureBounds.logDebug;
import static dev.detpikachu.structurebounds.commands.CommandErrors.ERR_NOT_A_PLAYER;
import static dev.detpikachu.structurebounds.commands.CommandErrors.ERR_REFRESH_TOO_SOON;

@ApiStatus.Internal
public final class CommandGuards {

    public static Player requireExecutor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getExecutor() instanceof Player player)) {
            throw ERR_NOT_A_PLAYER.create();
        }

        return player;
    }

    public static void requireRefreshReady(Player player, PlayerSettings settings) throws CommandSyntaxException {
        if (!settings.isEnabled() || BoundsManager.claimCommandRefresh(player)) {
            return;
        }

        logDebug("Refused a bounds command for {}: the refresh cooldown has not elapsed.", player.getName());

        throw ERR_REFRESH_TOO_SOON.create();
    }
}
