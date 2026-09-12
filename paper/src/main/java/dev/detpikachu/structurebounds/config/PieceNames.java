package dev.detpikachu.structurebounds.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class PieceNames {

    public static final String RESOURCE = "piece-names.yml";

    private static final PieceNames INSTANCE = new PieceNames();

    private Map<String, String> names = Map.of();

    public static PieceNames getInstance() {
        return INSTANCE;
    }

    public static void deserialize(FileConfiguration config) {
        final var names = new HashMap<String, String>();

        for (final var key : config.getKeys(false)) {
            final var name = config.getString(key);

            if (name != null && !name.isBlank()) {
                names.put(key, name);
            }
        }

        INSTANCE.names = Map.copyOf(names);
    }

    public @Nullable String find(String id, String shortId) {
        final var exact = this.names.get(id);

        return exact != null ? exact : this.names.get(shortId);
    }

    public int size() {
        return this.names.size();
    }
}
