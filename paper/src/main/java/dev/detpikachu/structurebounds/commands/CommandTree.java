package dev.detpikachu.structurebounds.commands;

import dev.detpikachu.structurebounds.commands.player.PlayerBoundsCommand;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CommandTree {

    public static void register(Commands registrar) {
        registrar.register(PlayerBoundsCommand.construct());
    }
}
