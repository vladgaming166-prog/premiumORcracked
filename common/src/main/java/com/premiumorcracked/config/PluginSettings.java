package com.premiumorcracked.config;

public record PluginSettings(
        boolean updatePrefixWithoutRestart,
        boolean cachePrefixes
) {
    public static PluginSettings defaults() {
        return new PluginSettings(true, true);
    }
}
