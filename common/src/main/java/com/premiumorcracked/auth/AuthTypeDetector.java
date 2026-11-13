package com.premiumorcracked.auth;

import com.premiumorcracked.api.AuthType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Derives {@link AuthType} from online-mode status and UUID shape.
 * Velocity remains the authority; Paper uses the same rules only as a fallback.
 */
public final class AuthTypeDetector {
    public @NotNull AuthType detect(boolean onlineMode, @Nullable UUID uniqueId, @Nullable String username) {
        if (onlineMode) {
            return AuthType.PREMIUM;
        }
        if (uniqueId == null || username == null || username.isBlank()) {
            return AuthType.UNKNOWN;
        }
        if (uniqueId.equals(OfflineUuids.fromName(username))) {
            return AuthType.CRACKED;
        }
        if (uniqueId.version() == 4) {
            return AuthType.PREMIUM;
        }
        return AuthType.UNKNOWN;
    }
}
