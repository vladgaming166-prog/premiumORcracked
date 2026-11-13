package com.premiumorcracked.prefix;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PrefixView;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record ResolvedPrefix(
        @NotNull AuthType type,
        boolean enabled,
        @NotNull String raw,
        @NotNull Component component,
        @NotNull String legacyAmpersand,
        @NotNull String legacySection
) {
    public @NotNull PrefixView toView() {
        return new PrefixView(type, enabled, raw, component, legacyAmpersand, legacySection);
    }
}
