package com.premiumorcracked.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ValidationResult(@NotNull List<String> errors) {
    public static @NotNull ValidationResult ok() {
        return new ValidationResult(List.of());
    }

    public static @NotNull ValidationResult failed(@NotNull List<String> errors) {
        return new ValidationResult(List.copyOf(errors));
    }

    public boolean success() {
        return errors.isEmpty();
    }

    public @NotNull String formattedErrors() {
        return String.join("; ", errors);
    }
}
