package dev.detpikachu.structurebounds.render;

import dev.detpikachu.structurebounds.player.PlayerSettings;
import dev.detpikachu.structurebounds.scan.ScannedStructure;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ApiStatus.Internal
public final class BoxSelector {

    public static Selection select(
            List<ScannedStructure> structures, PlayerSettings settings, int budget, boolean isGlowing, Vec3 position) {
        final var wanted = wantedBoxes(structures, settings, isGlowing);
        final var isTruncated = wanted.size() > budget;
        final var boxes = isTruncated ? wanted.subList(0, budget) : wanted;
        final var labels = wantedLabels(structures, settings, position, boxes);

        return new Selection(boxes, labels, wanted.size(), isTruncated);
    }

    private static List<BoxKey> wantedBoxes(
            List<ScannedStructure> structures, PlayerSettings settings, boolean isGlowing) {
        final var keys = new ArrayList<BoxKey>();

        for (final var structure : structures) {
            keys.add(new BoxKey(structure.bounds(), BoxColor.STRUCTURE, isGlowing));
        }

        for (final var structure : structures) {
            for (final var piece : shownPieces(structure, settings)) {
                keys.add(pieceKey(piece));
            }
        }

        return keys;
    }

    private static List<LabelKey> wantedLabels(
            List<ScannedStructure> structures, PlayerSettings settings, Vec3 position, List<BoxKey> boxes) {
        if (!settings.showLabels()) {
            return List.of();
        }

        final var drawn = new HashSet<>(boxes);
        final var labels = new ArrayList<LabelKey>();

        for (final var structure : structures) {
            for (final var piece : shownPieces(structure, settings)) {
                final var label = labelFor(piece, position);

                if (label != null && drawn.contains(pieceKey(piece))) {
                    labels.add(label);
                }
            }
        }

        return labels;
    }

    private static @Nullable LabelKey labelFor(ScannedStructure.Piece piece, Vec3 position) {
        final var name = piece.name();

        if (name == null || !LabelDisplays.isLegible(piece.bounds(), position)) {
            return null;
        }

        return new LabelKey(piece.bounds(), name);
    }

    private static BoxKey pieceKey(ScannedStructure.Piece piece) {
        return new BoxKey(piece.bounds(), piece.isStart() ? BoxColor.START : BoxColor.PIECE, false);
    }

    private static List<ScannedStructure.Piece> shownPieces(ScannedStructure structure, PlayerSettings settings) {
        final var pieces = structure.pieces();

        if (settings.showAllBoxes() || pieces.size() <= settings.boxThreshold()) {
            return pieces;
        }

        return pieces.subList(0, settings.boxThreshold());
    }

    @ApiStatus.Internal
    public record Selection(List<BoxKey> boxes, List<LabelKey> labels, int wantedCount, boolean isTruncated) {

        public List<DisplayKey> displays() {
            final var displays = new ArrayList<DisplayKey>(this.boxes.size() + this.labels.size());

            displays.addAll(this.boxes);
            displays.addAll(this.labels);

            return displays;
        }
    }
}
