package com.premiumorcracked.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Authentication type and prefix for one player.
 *
 * <p>The authentication prefix is intentionally separate from rank prefixes.
 * Callers that build a display string should prepend {@link #prefixLegacy()} to
 * an existing rank/name string instead of replacing it.
 */
public record PlayerPrefixInfo(
        @NotNull UUID uniqueId,
        @NotNull String username,
        @NotNull AuthType type,
        @NotNull PrefixView prefix
) {
    public @NotNull String prefixLegacy() {
        return prefix.visibleLegacyAmpersand();
    }

    public @NotNull String prefixSection() {
        return prefix.visibleLegacySection();
    }

    public @NotNull Component prefixComponent() {
        return prefix.visibleComponent();
    }

    /**
     * Prefix plus username. Does not include rank prefixes.
     */
    public @NotNull String displayLegacy() {
        return prefixLegacy() + username;
    }

    public @NotNull Component displayComponent() {
        return Component.empty().append(prefixComponent()).append(Component.text(username));
    }
}
