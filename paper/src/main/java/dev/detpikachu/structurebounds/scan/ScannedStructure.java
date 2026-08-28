package dev.detpikachu.structurebounds.scan;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public record ScannedStructure(BoundingBox bounds, List<Piece> pieces) {

    @ApiStatus.Internal
    public record Piece(BoundingBox bounds, boolean isStart) {}
}
