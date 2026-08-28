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
public final class PlayerBoundsAllPiecesCommand {

    private static final String CMD_ALL_PIECES = "all-pieces";

    public static LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(CMD_ALL_PIECES).executes(PlayerBoundsAllPiecesCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var settings = PlayerSettings.load(player);
        final var updated = settings.withShowAllBoxes(!settings.showAllBoxes());
        final var message = updated.showAllBoxes()
                ? "Every piece is drawn, whatever the threshold."
                : "Pieces past the threshold are hidden again.";

        requireRefreshReady(player, updated);
        BoundsManager.apply(player, updated);
        player.sendMessage(text(message, GRAY));

        return 1;
    }
}
