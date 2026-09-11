package dev.detpikachu.structurebounds.scan;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public record Scan(List<ScannedStructure> structures, boolean isRadiusComplete) {

    public Scan {
        structures = List.copyOf(structures);
    }
}
