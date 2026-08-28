package dev.detpikachu.structurebounds.player;

import dev.detpikachu.structurebounds.StructureBounds;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PlayerSettings(boolean isEnabled, int boxThreshold, boolean showAllBoxes, boolean showLabels) {

    public static final int MIN_BOX_THRESHOLD = 0;

    private static final boolean DEFAULT_ENABLED = false;
    private static final int DEFAULT_BOX_THRESHOLD = 32;
    private static final boolean DEFAULT_SHOW_ALL_BOXES = false;
    private static final boolean DEFAULT_SHOW_LABELS = true;

    private static final NamespacedKey KEY_ENABLED = new NamespacedKey(StructureBounds.getInstance(), "enabled");
    private static final NamespacedKey KEY_BOX_THRESHOLD =
            new NamespacedKey(StructureBounds.getInstance(), "box-threshold");
    private static final NamespacedKey KEY_SHOW_ALL_BOXES =
            new NamespacedKey(StructureBounds.getInstance(), "show-all-boxes");
    private static final NamespacedKey KEY_SHOW_LABELS =
            new NamespacedKey(StructureBounds.getInstance(), "show-labels");

    public static PlayerSettings load(Player player) {
        final var container = player.getPersistentDataContainer();

        return new PlayerSettings(
                container.getOrDefault(KEY_ENABLED, PersistentDataType.BOOLEAN, DEFAULT_ENABLED),
                container.getOrDefault(KEY_BOX_THRESHOLD, PersistentDataType.INTEGER, DEFAULT_BOX_THRESHOLD),
                container.getOrDefault(KEY_SHOW_ALL_BOXES, PersistentDataType.BOOLEAN, DEFAULT_SHOW_ALL_BOXES),
                container.getOrDefault(KEY_SHOW_LABELS, PersistentDataType.BOOLEAN, DEFAULT_SHOW_LABELS));
    }

    public void save(Player player) {
        final var container = player.getPersistentDataContainer();

        container.set(KEY_ENABLED, PersistentDataType.BOOLEAN, this.isEnabled);
        container.set(KEY_BOX_THRESHOLD, PersistentDataType.INTEGER, this.boxThreshold);
        container.set(KEY_SHOW_ALL_BOXES, PersistentDataType.BOOLEAN, this.showAllBoxes);
        container.set(KEY_SHOW_LABELS, PersistentDataType.BOOLEAN, this.showLabels);
    }

    public PlayerSettings withEnabled(boolean isEnabled) {
        return new PlayerSettings(isEnabled, this.boxThreshold, this.showAllBoxes, this.showLabels);
    }

    public PlayerSettings withBoxThreshold(int boxThreshold) {
        return new PlayerSettings(this.isEnabled, boxThreshold, this.showAllBoxes, this.showLabels);
    }

    public PlayerSettings withShowAllBoxes(boolean showAllBoxes) {
        return new PlayerSettings(this.isEnabled, this.boxThreshold, showAllBoxes, this.showLabels);
    }

    public PlayerSettings withShowLabels(boolean showLabels) {
        return new PlayerSettings(this.isEnabled, this.boxThreshold, this.showAllBoxes, showLabels);
    }
}
