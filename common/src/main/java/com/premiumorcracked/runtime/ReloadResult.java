package com.premiumorcracked.runtime;

import com.premiumorcracked.config.PluginConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ReloadResult(
        boolean success,
        @NotNull List<String> errors,
        @Nullable PluginConfiguration configuration,
        int backendsNotified
) {
    public static @NotNull ReloadResult success(@NotNull PluginConfiguration configuration, int backendsNotified) {
        return new ReloadResult(true, List.of(), configuration, backendsNotified);
    }

    public static @NotNull ReloadResult failure(@NotNull List<String> errors) {
        return new ReloadResult(false, List.copyOf(errors), null, 0);
    }

    public @NotNull String message() {
        if (success) {
            return "Reloaded prefixes and synchronized " + backendsNotified + " backend server(s).";
        }
        return "Reload failed: " + String.join("; ", errors);
    }
}
