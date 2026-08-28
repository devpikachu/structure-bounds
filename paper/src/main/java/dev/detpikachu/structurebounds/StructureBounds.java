package dev.detpikachu.structurebounds;

import dev.detpikachu.structurebounds.commands.CommandCooldown;
import dev.detpikachu.structurebounds.commands.CommandTree;
import dev.detpikachu.structurebounds.config.Options;
import dev.detpikachu.structurebounds.listeners.PaperListener;
import dev.detpikachu.structurebounds.render.BoundsManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Properties;

@ApiStatus.Internal
public final class StructureBounds extends JavaPlugin {

    private static final String PROPERTIES_RESOURCE = "structure-bounds.properties";
    private static final String KEY_MINECRAFT_VERSION = "minecraftVersion";

    public static ComponentLogger LOGGER = ComponentLogger.logger();

    public static StructureBounds getInstance() {
        return JavaPlugin.getPlugin(StructureBounds.class);
    }

    public static void logDebug(String message, Object... arguments) {
        if (Options.getInstance().isDebug()) {
            LOGGER.info(message, arguments);
        }
    }

    @Override
    public void onEnable() {
        LOGGER = this.getComponentLogger();
        this.warnOnVersionMismatch();

        this.saveDefaultConfig();
        Options.deserialize(this.getConfig());

        this.registerListeners();

        this.logStartupSummary();
    }

    @Override
    public void onDisable() {
        BoundsManager.stop();
        CommandCooldown.dropAll();
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new PaperListener(), this);
        this.getLifecycleManager()
                .registerEventHandler(LifecycleEvents.COMMANDS, commands -> CommandTree.register(commands.registrar()));
    }

    private void logStartupSummary() {
        LOGGER.info("Enabled for Minecraft {}.", this.getServer().getMinecraftVersion());
    }

    private void warnOnVersionMismatch() {
        final var targetVersion = this.getTargetMinecraftVersion();
        final var runningVersion = this.getServer().getMinecraftVersion();

        if (runningVersion.equals(targetVersion)) {
            return;
        }

        LOGGER.warn(
                "Structure Bounds targets Minecraft {} but this server runs {}. It relies on server internals, so features may misbehave or fail outright on another version.",
                targetVersion,
                runningVersion);
    }

    private @Nullable String getTargetMinecraftVersion() {
        final var properties = new Properties();

        try (var stream = this.getResource(PROPERTIES_RESOURCE)) {
            if (stream == null) {
                LOGGER.error("{} is missing from the JAR.", PROPERTIES_RESOURCE);
                return null;
            }

            properties.load(stream);
        } catch (IOException exception) {
            LOGGER.error("Could not read {} from the JAR.", PROPERTIES_RESOURCE, exception);
            return null;
        }

        return properties.getProperty(KEY_MINECRAFT_VERSION);
    }
}
