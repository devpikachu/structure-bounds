package dev.detpikachu.structurebounds.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class Options extends OptionsBase {

    private static final Options INSTANCE = new Options();

    private static final boolean DEFAULT_DEBUG = false;

    private static final String KEY_DEBUG = "debug";

    private boolean isDebug = DEFAULT_DEBUG;

    public static Options getInstance() {
        return INSTANCE;
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public static void deserialize(FileConfiguration config) {
        INSTANCE.isDebug = config.getBoolean(KEY_DEBUG, DEFAULT_DEBUG);
    }
}
