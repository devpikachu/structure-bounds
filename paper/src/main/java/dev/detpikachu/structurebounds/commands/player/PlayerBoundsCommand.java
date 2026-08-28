package dev.detpikachu.structurebounds.commands.player;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.detpikachu.structurebounds.Permissions;
import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.render.BoundsManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;

import static dev.detpikachu.structurebounds.commands.CommandGuards.requireExecutor;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@ApiStatus.Internal
public final class PlayerBoundsCommand {

    private static final String CMD_BOUNDS = "bounds";

    public static LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal(CMD_BOUNDS)
                .requires(PlayerBoundsCommand::isAllowed)
                .executes(PlayerBoundsCommand::execute)
                .then(PlayerBoundsAllBoxesCommand.construct())
                .then(PlayerBoundsBoxesCommand.construct())
                .build();
    }

    private static boolean isAllowed(CommandSourceStack stack) {
        return stack.getSender().hasPermission(Permissions.USE);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = requireExecutor(context);
        final var settings = PlayerSettings.load(player);
        final var updated = settings.withEnabled(!settings.isEnabled());
        final var message = updated.isEnabled() ? "Structure bounds shown." : "Structure bounds hidden.";

        updated.save(player);
        BoundsManager.refresh(player);
        player.sendMessage(text(message, GRAY));

        return 1;
    }
}
