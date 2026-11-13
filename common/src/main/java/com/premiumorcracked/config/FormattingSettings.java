package com.premiumorcracked.config;

public record FormattingSettings(
        boolean colorSupport,
        boolean legacyColors,
        boolean miniMessage
) {
    public static FormattingSettings defaults() {
        return new FormattingSettings(true, true, true);
    }
}
