package dev.detpikachu.structurebounds.commands.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.render.BoundsManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.commands.CommandGuards.requireExecutor;
import static dev.detpikachu.structurebounds.commands.CommandGuards.requireRefreshReady;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class PlayerBoundsShowLabelNamesCommand {

    private static final String CMD_NAMES = "names";

    public static LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(CMD_NAMES).executes(PlayerBoundsShowLabelNamesCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var settings = PlayerSettings.load(player);
        final var updated = settings.withShowLabelNames(!settings.showLabelNames());
        final var message =
                updated.showLabelNames() ? "Piece friendly names are shown." : "Piece friendly names are hidden.";

        requireRefreshReady(player, updated);
        BoundsManager.apply(player, updated);
        player.sendMessage(text(message, GRAY));

        return 1;
    }
}
