package com.premiumorcracked.auth;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class OfflineUuids {
    private OfflineUuids() {
    }

    public static @NotNull UUID fromName(@NotNull String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }
}
