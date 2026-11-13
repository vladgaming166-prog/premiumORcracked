package com.premiumorcracked.placeholder;

import com.premiumorcracked.api.PlayerPrefixInfo;
import com.premiumorcracked.api.PremiumOrCrackedApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Shared PlaceholderAPI identifiers:
 * {@code %premiumorcracked_prefix%}, {@code %premiumorcracked_type%}, {@code %premiumorcracked_display%}.
 */
public final class PrefixPlaceholders {
    public static final String IDENTIFIER = "premiumorcracked";
    public static final String PREFIX = "prefix";
    public static final String TYPE = "type";
    public static final String DISPLAY = "display";

    private PrefixPlaceholders() {
    }

    public static @Nullable String resolve(@Nullable String params, @NotNull PlayerPrefixInfo info) {
        if (params == null || params.isBlank()) {
            return info.prefixLegacy();
        }
        return switch (params.toLowerCase(Locale.ROOT).trim()) {
            case PREFIX -> info.prefixLegacy();
            case TYPE -> info.type().name();
            case DISPLAY -> info.displayLegacy();
            default -> null;
        };
    }

    public static @Nullable String resolve(
            @Nullable String params,
            @NotNull PremiumOrCrackedApi api,
            @NotNull UUID playerId,
            @Nullable String username
    ) {
        PlayerPrefixInfo info = api.getPlayerInfo(playerId).orElse(null);
        if (info == null) {
            info = new PlayerPrefixInfo(
                    playerId,
                    username == null ? playerId.toString() : username,
                    api.getAuthTypeOrUnknown(playerId),
                    api.getPrefix(playerId)
            );
        }
        return resolve(params, info);
    }
}
