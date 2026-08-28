package dev.detpikachu.structurebounds.render;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum BoxColor {
    STRUCTURE,
    START,
    PIECE;

    public BlockState getBlockState() {
        return switch (this) {
            case STRUCTURE -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case START -> Blocks.LIME_CONCRETE.defaultBlockState();
            case PIECE -> Blocks.BLUE_CONCRETE.defaultBlockState();
        };
    }
}
