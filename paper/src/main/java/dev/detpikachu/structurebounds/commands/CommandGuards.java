package dev.detpikachu.structurebounds.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.commands.CommandErrors.ERR_NOT_A_PLAYER;

@ApiStatus.Internal
public final class CommandGuards {

    public static Player requireExecutor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getExecutor() instanceof Player player)) {
            throw ERR_NOT_A_PLAYER.create();
        }

        return player;
    }
}
