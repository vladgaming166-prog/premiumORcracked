package com.premiumorcracked.config;

public record DisplaySettings(
        boolean tablist,
        boolean chat,
        boolean nametag,
        boolean scoreboard
) {
    public static DisplaySettings defaults() {
        return new DisplaySettings(true, false, false, false);
    }
}
