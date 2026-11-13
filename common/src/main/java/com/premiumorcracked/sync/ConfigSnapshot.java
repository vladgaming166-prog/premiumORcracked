package com.premiumorcracked.sync;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.config.DisplaySettings;
import com.premiumorcracked.config.FormattingSettings;
import com.premiumorcracked.config.PluginConfiguration;
import com.premiumorcracked.config.PluginSettings;
import com.premiumorcracked.config.PrefixDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wire copy of the Velocity configuration so every Paper backend uses the same prefixes.
 */
public final class ConfigSnapshot {
    public static final int VERSION = 1;

    public int version = VERSION;
    public Map<String, PrefixWire> prefixes = new LinkedHashMap<>();
    public boolean tablist = true;
    public boolean chat;
    public boolean nametag;
    public boolean scoreboard;
    public boolean colorSupport = true;
    public boolean legacyColors = true;
    public boolean miniMessage = true;
    public boolean updatePrefixWithoutRestart = true;
    public boolean cachePrefixes = true;

    public static @NotNull ConfigSnapshot from(@NotNull PluginConfiguration configuration) {
        ConfigSnapshot snapshot = new ConfigSnapshot();
        for (AuthType type : AuthType.values()) {
            PrefixDefinition definition = configuration.prefix(type);
            snapshot.prefixes.put(type.key(), new PrefixWire(definition.enabled(), definition.value()));
        }
        snapshot.tablist = configuration.display().tablist();
        snapshot.chat = configuration.display().chat();
        snapshot.nametag = configuration.display().nametag();
        snapshot.scoreboard = configuration.display().scoreboard();
        snapshot.colorSupport = configuration.formatting().colorSupport();
        snapshot.legacyColors = configuration.formatting().legacyColors();
        snapshot.miniMessage = configuration.formatting().miniMessage();
        snapshot.updatePrefixWithoutRestart = configuration.settings().updatePrefixWithoutRestart();
        snapshot.cachePrefixes = configuration.settings().cachePrefixes();
        return snapshot;
    }

    public @NotNull PluginConfiguration toConfiguration() {
        EnumMap<AuthType, PrefixDefinition> mapped = new EnumMap<>(AuthType.class);
        PluginConfiguration defaults = PluginConfiguration.defaults();
        for (AuthType type : AuthType.values()) {
            PrefixWire wire = prefixes.get(type.key());
            if (wire == null) {
                mapped.put(type, defaults.prefix(type));
            } else {
                mapped.put(type, new PrefixDefinition(wire.enabled, wire.value == null ? "" : wire.value));
            }
        }
        return new PluginConfiguration(
                mapped,
                new DisplaySettings(tablist, chat, nametag, scoreboard),
                new FormattingSettings(colorSupport, legacyColors, miniMessage),
                new PluginSettings(updatePrefixWithoutRestart, cachePrefixes)
        );
    }

    public static final class PrefixWire {
        public boolean enabled;
        public String value = "";

        public PrefixWire() {
        }

        public PrefixWire(boolean enabled, String value) {
            this.enabled = enabled;
            this.value = value;
        }
    }
}
