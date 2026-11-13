package com.premiumorcracked.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Prepends the authentication prefix without replacing rank prefixes or names.
 */
public final class DisplayComposer {
    private DisplayComposer() {
    }

    public static @NotNull String prepend(@Nullable String authPrefix, @Nullable String existingDisplay) {
        String existing = existingDisplay == null ? "" : existingDisplay;
        if (authPrefix == null || authPrefix.isEmpty()) {
            return existing;
        }
        if (existing.startsWith(authPrefix)) {
            return existing;
        }
        return authPrefix + existing;
    }

    public static @NotNull Component prepend(@Nullable Component authPrefix, @Nullable Component existingDisplay) {
        Component existing = existingDisplay == null ? Component.empty() : existingDisplay;
        Component prefix = authPrefix == null ? Component.empty() : authPrefix;
        if (prefix.equals(Component.empty())) {
            return existing;
        }
        if (startsWith(existing, prefix)) {
            return existing;
        }
        return Component.empty().append(prefix).append(existing);
    }

    public static boolean startsWith(@NotNull Component display, @NotNull Component prefix) {
        if (prefix.equals(Component.empty())) {
            return true;
        }
        if (display.equals(prefix)) {
            return true;
        }
        if (display instanceof TextComponent text && prefix instanceof TextComponent prefixText) {
            String displayContent = text.content();
            String prefixContent = prefixText.content();
            if (!prefixContent.isEmpty() && displayContent.startsWith(prefixContent) && text.children().isEmpty()) {
                return true;
            }
        }
        if (!display.children().isEmpty() && display.children().getFirst().equals(prefix)) {
            return true;
        }
        return false;
    }
}
