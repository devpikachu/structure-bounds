package dev.detpikachu.structurebounds.render;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record BoxKey(BoundingBox bounds, BoxColor color) {}
