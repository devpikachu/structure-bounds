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
import static dev.detpikachu.structurebounds.player.PlayerSettings.MIN_BOX_THRESHOLD;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class PlayerBoundsBoxesCommand {

    private static final String CMD_BOXES = "boxes";

    private static final String ARG_COUNT = "count";

    public static LiteralArgumentBuilder<CommandSourceStack> construct() {
        final var count = Commands.argument(
                        ARG_COUNT,
                        IntegerArgumentType.integer(
                                MIN_BOX_THRESHOLD, Options.getInstance().getMaxBoxesPerPlayer()))
                .executes(PlayerBoundsBoxesCommand::execute);

        return Commands.literal(CMD_BOXES).then(count);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var count = context.getArgument(ARG_COUNT, int.class);
        final var updated = PlayerSettings.load(player).withBoxThreshold(count);

        updated.save(player);
        BoundsManager.refresh(player);
        player.sendMessage(text("Structures of up to ")
                .append(text(count))
                .append(text(" boxes now draw in full."))
                .color(GRAY));

        return 1;
    }
}
