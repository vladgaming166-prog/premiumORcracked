package com.premiumorcracked.sync;

import com.premiumorcracked.api.AuthType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayerAuthRecord(
        @NotNull UUID uniqueId,
        @NotNull String username,
        @NotNull AuthType type
) {
}
