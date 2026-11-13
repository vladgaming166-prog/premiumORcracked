package com.premiumorcracked.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PrefixDefinition(boolean enabled, @NotNull String value) {
    public static @NotNull PrefixDefinition defaultsFor(boolean enabled, @NotNull String value) {
        return new PrefixDefinition(enabled, value);
    }

    public static @NotNull PrefixDefinition disabled() {
        return new PrefixDefinition(false, "");
    }

    public @NotNull PrefixDefinition withValue(@Nullable String newValue) {
        return new PrefixDefinition(enabled, newValue == null ? "" : newValue);
    }

    public boolean visible() {
        return enabled && !value.isEmpty();
    }
}
