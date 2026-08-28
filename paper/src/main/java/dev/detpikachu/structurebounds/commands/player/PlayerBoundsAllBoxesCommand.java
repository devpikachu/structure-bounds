package dev.detpikachu.structurebounds.commands.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.commands.CommandGuards.requireExecutor;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class PlayerBoundsAllBoxesCommand {

    private static final String CMD_ALL_BOXES = "allboxes";

    public static LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(CMD_ALL_BOXES).executes(PlayerBoundsAllBoxesCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var settings = PlayerSettings.load(player);
        final var updated = settings.withShowAllBoxes(!settings.showAllBoxes());
        final var message = updated.showAllBoxes()
                ? "Every box is drawn, whatever the threshold."
                : "Boxes past the threshold are hidden again.";

        updated.save(player);
        player.sendMessage(text(message, GRAY));

        return 1;
    }
}
