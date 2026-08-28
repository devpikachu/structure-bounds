package dev.detpikachu.structurebounds.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class Options extends OptionsBase {

    private static final Options INSTANCE = new Options();

    private static final boolean DEFAULT_DEBUG = false;
    private static final int DEFAULT_SCAN_RADIUS_CHUNKS = 6;
    private static final int DEFAULT_MAX_BOXES_PER_PLAYER = 256;

    private static final int MIN_SCAN_RADIUS_CHUNKS = 1;
    private static final int MAX_SCAN_RADIUS_CHUNKS = 16;
    private static final int MIN_MAX_BOXES_PER_PLAYER = 1;
    private static final int MAX_MAX_BOXES_PER_PLAYER = 1024;

    private static final String KEY_DEBUG = "debug";
    private static final String KEY_SCAN_RADIUS_CHUNKS = "scan-radius-chunks";
    private static final String KEY_MAX_BOXES_PER_PLAYER = "max-boxes-per-player";

    private boolean isDebug = DEFAULT_DEBUG;
    private int scanRadiusChunks = DEFAULT_SCAN_RADIUS_CHUNKS;
    private int maxBoxesPerPlayer = DEFAULT_MAX_BOXES_PER_PLAYER;

    public static Options getInstance() {
        return INSTANCE;
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public int getScanRadiusChunks() {
        return this.scanRadiusChunks;
    }

    public int getMaxBoxesPerPlayer() {
        return this.maxBoxesPerPlayer;
    }

    public static void deserialize(FileConfiguration config) {
        INSTANCE.isDebug = config.getBoolean(KEY_DEBUG, DEFAULT_DEBUG);
        INSTANCE.scanRadiusChunks = inRange(
                config,
                KEY_SCAN_RADIUS_CHUNKS,
                DEFAULT_SCAN_RADIUS_CHUNKS,
                MIN_SCAN_RADIUS_CHUNKS,
                MAX_SCAN_RADIUS_CHUNKS);
        INSTANCE.maxBoxesPerPlayer = inRange(
                config,
                KEY_MAX_BOXES_PER_PLAYER,
                DEFAULT_MAX_BOXES_PER_PLAYER,
                MIN_MAX_BOXES_PER_PLAYER,
                MAX_MAX_BOXES_PER_PLAYER);
    }
}
