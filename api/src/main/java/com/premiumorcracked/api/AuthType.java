package com.premiumorcracked.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Session authentication type determined by the Velocity proxy.
 */
public enum AuthType {
    PREMIUM,
    CRACKED,
    UNKNOWN;

    public @NotNull String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static @NotNull AuthType fromKey(@Nullable String key) {
        if (key == null || key.isBlank()) {
            return UNKNOWN;
        }
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "premium", "p", "online" -> PREMIUM;
            case "cracked", "c", "offline" -> CRACKED;
            default -> UNKNOWN;
        };
    }
}
