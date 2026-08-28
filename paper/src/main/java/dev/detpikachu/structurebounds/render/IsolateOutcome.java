package dev.detpikachu.structurebounds.render;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum IsolateOutcome {
    HELD,
    HELD_NEAREST,
    RELEASED,
    NOT_INSIDE
}
