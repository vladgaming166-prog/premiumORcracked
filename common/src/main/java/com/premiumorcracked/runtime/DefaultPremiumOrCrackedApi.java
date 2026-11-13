package com.premiumorcracked.runtime;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PlayerPrefixInfo;
import com.premiumorcracked.api.PremiumOrCrackedApi;
import com.premiumorcracked.api.PrefixView;
import com.premiumorcracked.display.DisplayComposer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class DefaultPremiumOrCrackedApi implements PremiumOrCrackedApi {
    private final PrefixRuntime runtime;

    public DefaultPremiumOrCrackedApi(@NotNull PrefixRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public @NotNull Optional<AuthType> getAuthType(@NotNull UUID playerId) {
        return runtime.store().get(playerId).map(player -> player.type());
    }

    @Override
    public @NotNull Optional<AuthType> getAuthType(@NotNull String playerName) {
        return runtime.store().getByName(playerName).map(player -> player.type());
    }

    @Override
    public @NotNull AuthType getAuthTypeOrUnknown(@NotNull UUID playerId) {
        return runtime.store().typeOrUnknown(playerId);
    }

    @Override
    public @NotNull PrefixView getPrefix(@NotNull AuthType type) {
        return runtime.prefix(type);
    }

    @Override
    public @NotNull PrefixView getPrefix(@NotNull UUID playerId) {
        return runtime.prefix(playerId);
    }

    @Override
    public @NotNull String getPrefixLegacy(@NotNull UUID playerId) {
        return runtime.prefix(playerId).visibleLegacyAmpersand();
    }

    @Override
    public @NotNull Component getPrefixComponent(@NotNull UUID playerId) {
        return runtime.prefix(playerId).visibleComponent();
    }

    @Override
    public @NotNull String getTypeName(@NotNull UUID playerId) {
        return getAuthTypeOrUnknown(playerId).name();
    }

    @Override
    public @NotNull String getDisplayLegacy(@NotNull UUID playerId) {
        return runtime.info(playerId, null).displayLegacy();
    }

    @Override
    public @NotNull Component getDisplayComponent(@NotNull UUID playerId) {
        return runtime.info(playerId, null).displayComponent();
    }

    @Override
    public @NotNull Optional<PlayerPrefixInfo> getPlayerInfo(@NotNull UUID playerId) {
        return runtime.store().info(playerId, runtime.registry());
    }

    @Override
    public @NotNull Optional<PlayerPrefixInfo> getPlayerInfo(@NotNull String playerName) {
        return runtime.store().getByName(playerName)
                .flatMap(player -> runtime.store().info(player.uniqueId(), runtime.registry()));
    }

    @Override
    public boolean isPrefixEnabled(@NotNull AuthType type) {
        return runtime.configuration().prefix(type).enabled();
    }

    @Override
    public @NotNull String prependToExisting(@NotNull UUID playerId, @Nullable String existingDisplay) {
        return DisplayComposer.prepend(getPrefixLegacy(playerId), existingDisplay);
    }

    @Override
    public @NotNull Component prependToExisting(@NotNull UUID playerId, @Nullable Component existingDisplay) {
        return DisplayComposer.prepend(getPrefixComponent(playerId), existingDisplay);
    }
}
