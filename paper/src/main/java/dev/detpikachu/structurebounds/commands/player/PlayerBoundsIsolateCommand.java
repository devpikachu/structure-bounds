package dev.detpikachu.structurebounds.commands.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.render.BoundsManager;
import dev.detpikachu.structurebounds.render.IsolateOutcome;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.commands.CommandGuards.requireBoundsShown;
import static dev.detpikachu.structurebounds.commands.CommandGuards.requireExecutor;
import static dev.detpikachu.structurebounds.commands.CommandGuards.requireRefreshReady;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class PlayerBoundsIsolateCommand {

    private static final String CMD_ISOLATE = "isolate";

    public static LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(CMD_ISOLATE).executes(PlayerBoundsIsolateCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var settings = PlayerSettings.load(player);

        requireBoundsShown(settings);
        requireRefreshReady(player, settings);

        final var message = describe(BoundsManager.isolate(player));

        player.sendMessage(text(message, GRAY));

        return 1;
    }

    private static String describe(IsolateOutcome outcome) {
        return switch (outcome) {
            case HELD -> "Isolated to the structure you are standing in.";
            case HELD_NEAREST -> "You are standing in more than one structure. Isolated to the nearest one.";
            case RELEASED -> "Isolation cleared. Every structure nearby is drawn again.";
            case NOT_INSIDE -> "You are not standing in a structure. Nothing to isolate.";
        };
    }
}
