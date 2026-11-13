package com.premiumorcracked.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Resolved authentication prefix for one {@link AuthType}.
 *
 * <p>This value is independent of LuckPerms prefixes, suffixes and chat formats.
 */
public record PrefixView(
        @NotNull AuthType type,
        boolean enabled,
        @NotNull String rawValue,
        @NotNull Component component,
        @NotNull String legacyAmpersand,
        @NotNull String legacySection
) {
    public static @NotNull PrefixView empty(@NotNull AuthType type) {
        return new PrefixView(type, false, "", Component.empty(), "", "");
    }

    /**
     * Prefix that should be shown. Empty when the type is disabled or the value is blank.
     */
    public @NotNull Component visibleComponent() {
        return isVisible() ? component : Component.empty();
    }

    public @NotNull String visibleLegacyAmpersand() {
        return isVisible() ? legacyAmpersand : "";
    }

    public @NotNull String visibleLegacySection() {
        return isVisible() ? legacySection : "";
    }

    public boolean isVisible() {
        return enabled && !rawValue.isEmpty();
    }
}
