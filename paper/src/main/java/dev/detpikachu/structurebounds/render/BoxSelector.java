package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.scan.ScannedStructure;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class BoxSelector {

    public static Selection select(List<ScannedStructure> structures, PlayerSettings settings, int budget) {
        final var wanted = wantedBoxes(structures, settings);
        final var isTruncated = wanted.size() > budget;

        return new Selection(isTruncated ? wanted.subList(0, budget) : wanted, wanted.size(), isTruncated);
    }

    private static List<BoxKey> wantedBoxes(List<ScannedStructure> structures, PlayerSettings settings) {
        final var keys = new ArrayList<BoxKey>();

        for (final var structure : structures) {
            keys.add(new BoxKey(structure.bounds(), BoxColor.STRUCTURE));
        }

        for (final var structure : structures) {
            for (final var piece : shownPieces(structure, settings)) {
                keys.add(new BoxKey(piece.bounds(), piece.isStart() ? BoxColor.START : BoxColor.PIECE));
            }
        }

        return keys;
    }

    private static List<ScannedStructure.Piece> shownPieces(ScannedStructure structure, PlayerSettings settings) {
        final var pieces = structure.pieces();

        if (settings.showAllBoxes() || pieces.size() <= settings.boxThreshold()) {
            return pieces;
        }

        return pieces.subList(0, settings.boxThreshold());
    }

    @ApiStatus.Internal
    public record Selection(List<BoxKey> boxes, int wantedCount, boolean isTruncated) {}
}
