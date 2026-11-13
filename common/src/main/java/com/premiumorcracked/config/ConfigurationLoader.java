package com.premiumorcracked.config;

import com.premiumorcracked.api.AuthType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Loads {@code config.yml} without requiring a plugin rebuild.
 */
public final class ConfigurationLoader {
    public static final String FILE_NAME = "config.yml";
    private static final String DEFAULT_RESOURCE = "/config.yml";

    private final Yaml yaml;

    public ConfigurationLoader() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        this.yaml = new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
    }

    public void copyDefaultIfMissing(@NotNull Path file) throws ConfigurationException {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.exists(file)) {
                return;
            }
            try (InputStream in = defaultResource()) {
                Files.copy(in, file);
            }
        } catch (IOException ex) {
            throw new ConfigurationException("Could not create " + file.getFileName() + ": " + ex.getMessage(), ex);
        }
    }

    public @NotNull PluginConfiguration load(@NotNull Path file) throws ConfigurationException {
        copyDefaultIfMissing(file);
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return load(reader);
        } catch (IOException ex) {
            throw new ConfigurationException("Could not read " + file.getFileName() + ": " + ex.getMessage(), ex);
        }
    }

    public @NotNull PluginConfiguration load(@NotNull Reader reader) throws ConfigurationException {
        Object loaded;
        try {
            loaded = yaml.load(reader);
        } catch (RuntimeException ex) {
            throw new ConfigurationException("Invalid YAML: " + ex.getMessage(), ex);
        }
        if (loaded == null) {
            return PluginConfiguration.defaults();
        }
        if (!(loaded instanceof Map<?, ?>)) {
            throw new ConfigurationException("config.yml must be a YAML mapping at the root");
        }
        return fromMap(YamlMaps.asMap(loaded));
    }

    public @NotNull PluginConfiguration loadDefaultResource() throws ConfigurationException {
        try (InputStream in = defaultResource()) {
            Object loaded = yaml.load(in);
            return fromMap(YamlMaps.asMap(loaded));
        } catch (IOException ex) {
            throw new ConfigurationException("Could not read bundled config.yml", ex);
        }
    }

    public @NotNull PluginConfiguration fromMap(@Nullable Map<String, Object> root) {
        Map<String, Object> safeRoot = root == null ? Map.of() : root;
        PluginConfiguration defaults = PluginConfiguration.defaults();

        Map<String, Object> prefixesRoot = YamlMaps.asMap(YamlMaps.first(safeRoot, "prefixes"));
        EnumMap<AuthType, PrefixDefinition> prefixes = new EnumMap<>(AuthType.class);
        for (AuthType type : AuthType.values()) {
            PrefixDefinition fallback = defaults.prefix(type);
            Map<String, Object> node = YamlMaps.asMap(YamlMaps.first(prefixesRoot, type.key()));
            boolean enabled = YamlMaps.bool(node, "enabled", fallback.enabled());
            String value = YamlMaps.string(node, "value", fallback.value());
            prefixes.put(type, new PrefixDefinition(enabled, value));
        }

        Map<String, Object> displayRoot = YamlMaps.asMap(YamlMaps.first(safeRoot, "display"));
        DisplaySettings display = new DisplaySettings(
                YamlMaps.bool(displayRoot, "tablist", defaults.display().tablist()),
                YamlMaps.bool(displayRoot, "chat", defaults.display().chat()),
                YamlMaps.bool(displayRoot, "nametag", defaults.display().nametag()),
                YamlMaps.bool(displayRoot, "scoreboard", defaults.display().scoreboard())
        );

        Map<String, Object> formattingRoot = YamlMaps.asMap(YamlMaps.first(safeRoot, "formatting"));
        FormattingSettings formatting = new FormattingSettings(
                YamlMaps.bool(formattingRoot, "color-support", defaults.formatting().colorSupport()),
                YamlMaps.bool(formattingRoot, "legacy-colors", defaults.formatting().legacyColors()),
                YamlMaps.bool(formattingRoot, "minimessage", defaults.formatting().miniMessage())
        );

        Map<String, Object> settingsRoot = YamlMaps.asMap(YamlMaps.first(safeRoot, "settings"));
        PluginSettings settings = new PluginSettings(
                YamlMaps.bool(settingsRoot, "update-prefix-without-restart", defaults.settings().updatePrefixWithoutRestart()),
                YamlMaps.bool(settingsRoot, "cache-prefixes", defaults.settings().cachePrefixes())
        );

        return new PluginConfiguration(prefixes, display, formatting, settings);
    }

    private static @NotNull InputStream defaultResource() throws ConfigurationException {
        InputStream in = ConfigurationLoader.class.getResourceAsStream(DEFAULT_RESOURCE);
        if (in == null) {
            throw new ConfigurationException("Bundled config.yml is missing from the plugin jar");
        }
        return in;
    }

    public static @NotNull String bundledDefaultYaml() throws ConfigurationException {
        try (InputStream in = defaultResource()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ConfigurationException("Could not read bundled config.yml", ex);
        }
    }

    public static @NotNull Path configFile(@NotNull Path dataDirectory) {
        return Objects.requireNonNull(dataDirectory).resolve(FILE_NAME);
    }
}
