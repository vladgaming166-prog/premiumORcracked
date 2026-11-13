package com.premiumorcracked.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Service locator so other plugins can obtain {@link PremiumOrCrackedApi}
 * without depending on Velocity or Paper internals.
 */
public final class PremiumOrCrackedProvider {
    private static volatile PremiumOrCrackedApi instance;

    private PremiumOrCrackedProvider() {
    }

    public static void register(@NotNull PremiumOrCrackedApi api) {
        instance = Objects.requireNonNull(api, "api");
    }

    public static void unregister(@Nullable PremiumOrCrackedApi api) {
        if (api == null || api == instance) {
            instance = null;
        }
    }

    public static @NotNull PremiumOrCrackedApi get() {
        PremiumOrCrackedApi api = instance;
        if (api == null) {
            throw new IllegalStateException("PremiumOrCracked is not loaded yet");
        }
        return api;
    }

    public static @NotNull Optional<PremiumOrCrackedApi> optional() {
        return Optional.ofNullable(instance);
    }

    public static boolean isRegistered() {
        return instance != null;
    }
}
