package com.premiumorcracked.config;

import com.premiumorcracked.api.AuthType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class PluginConfiguration {
    private final Map<AuthType, PrefixDefinition> prefixes;
    private final DisplaySettings display;
    private final FormattingSettings formatting;
    private final PluginSettings settings;

    public PluginConfiguration(
            Map<AuthType, PrefixDefinition> prefixes,
            DisplaySettings display,
            FormattingSettings formatting,
            PluginSettings settings
    ) {
        EnumMap<AuthType, PrefixDefinition> copy = new EnumMap<>(AuthType.class);
        for (AuthType type : AuthType.values()) {
            copy.put(type, prefixes.getOrDefault(type, PrefixDefinition.disabled()));
        }
        this.prefixes = Collections.unmodifiableMap(copy);
        this.display = Objects.requireNonNull(display);
        this.formatting = Objects.requireNonNull(formatting);
        this.settings = Objects.requireNonNull(settings);
    }

    public static @NotNull PluginConfiguration defaults() {
        EnumMap<AuthType, PrefixDefinition> prefixes = new EnumMap<>(AuthType.class);
        prefixes.put(AuthType.PREMIUM, new PrefixDefinition(true, "&7[&aP&7] "));
        prefixes.put(AuthType.CRACKED, new PrefixDefinition(true, "&7[&fC&7] "));
        prefixes.put(AuthType.UNKNOWN, new PrefixDefinition(true, "&7[&8?&7] "));
        return new PluginConfiguration(
                prefixes,
                DisplaySettings.defaults(),
                FormattingSettings.defaults(),
                PluginSettings.defaults()
        );
    }

    public @NotNull PrefixDefinition prefix(@NotNull AuthType type) {
        return prefixes.get(type);
    }

    public @NotNull Map<AuthType, PrefixDefinition> prefixes() {
        return prefixes;
    }

    public @NotNull DisplaySettings display() {
        return display;
    }

    public @NotNull FormattingSettings formatting() {
        return formatting;
    }

    public @NotNull PluginSettings settings() {
        return settings;
    }
}
