package dev.detpikachu.structurebounds.commands;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import org.jetbrains.annotations.ApiStatus;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@ApiStatus.Internal
public final class CommandErrors {

    public static final SimpleCommandExceptionType ERR_NOT_A_PLAYER =
            new SimpleCommandExceptionType(MessageComponentSerializer.message()
                    .serialize(text(
                            "Command must be executed by or as a real player. Executing from console without /execute isn't supported. Executing as entities other than players is not supported.",
                            RED)));
}
