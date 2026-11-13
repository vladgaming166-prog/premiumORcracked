package com.premiumorcracked.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Central API for authentication types and independent authentication prefixes.
 *
 * <p>Velocity is the source of truth. Paper backends receive the same type and
 * prefix values so every connected server shows the same indicator.
 *
 * <p>This API never writes LuckPerms (or other rank plugin) prefixes, suffixes
 * or chat formats.
 */
public interface PremiumOrCrackedApi {
    @NotNull Optional<AuthType> getAuthType(@NotNull UUID playerId);

    @NotNull Optional<AuthType> getAuthType(@NotNull String playerName);

    @NotNull AuthType getAuthTypeOrUnknown(@NotNull UUID playerId);

    @NotNull PrefixView getPrefix(@NotNull AuthType type);

    @NotNull PrefixView getPrefix(@NotNull UUID playerId);

    @NotNull String getPrefixLegacy(@NotNull UUID playerId);

    @NotNull Component getPrefixComponent(@NotNull UUID playerId);

    @NotNull String getTypeName(@NotNull UUID playerId);

    @NotNull String getDisplayLegacy(@NotNull UUID playerId);

    @NotNull Component getDisplayComponent(@NotNull UUID playerId);

    @NotNull Optional<PlayerPrefixInfo> getPlayerInfo(@NotNull UUID playerId);

    @NotNull Optional<PlayerPrefixInfo> getPlayerInfo(@NotNull String playerName);

    boolean isPrefixEnabled(@NotNull AuthType type);

    /**
     * Prepends the authentication prefix to an existing rank/name display
     * without removing that existing text.
     */
    @NotNull String prependToExisting(@NotNull UUID playerId, @Nullable String existingDisplay);

    @NotNull Component prependToExisting(@NotNull UUID playerId, @Nullable Component existingDisplay);
}
