package dev.detpikachu.structurebounds.commands.player;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.detpikachu.structurebounds.config.Options;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.render.BoundsManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.commands.CommandGuards.requireExecutor;
import static dev.detpikachu.structurebounds.commands.CommandGuards.requireRefreshReady;
import static dev.detpikachu.structurebounds.player.PlayerSettings.MIN_BOX_THRESHOLD;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class PlayerBoundsMaxPiecesCommand {

    private static final String CMD_MAX_PIECES = "max-pieces";

    private static final String ARG_COUNT = "count";

    public static LiteralArgumentBuilder<CommandSourceStack> construct() {
        final var count = Commands.argument(
                        ARG_COUNT,
                        IntegerArgumentType.integer(
                                MIN_BOX_THRESHOLD, Options.getInstance().getMaxBoxesPerPlayer()))
                .executes(PlayerBoundsMaxPiecesCommand::execute);

        return Commands.literal(CMD_MAX_PIECES).then(count);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var count = context.getArgument(ARG_COUNT, int.class);
        final var updated = PlayerSettings.load(player).withBoxThreshold(count);

        requireRefreshReady(player, updated);

        updated.save(player);
        BoundsManager.reconcile(player);
        player.sendMessage(text("Structures now draw up to ")
                .append(text(count))
                .append(text(" pieces."))
                .color(GRAY));

        return 1;
    }
}
