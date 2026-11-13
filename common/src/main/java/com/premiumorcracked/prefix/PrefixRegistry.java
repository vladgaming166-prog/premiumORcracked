package com.premiumorcracked.prefix;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PrefixView;
import com.premiumorcracked.config.PluginConfiguration;
import com.premiumorcracked.config.PrefixDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Renders and optionally caches prefixes from the live configuration.
 */
public final class PrefixRegistry {
    private final PrefixFormatter formatter;
    private final AtomicReference<PluginConfiguration> configuration;
    private final AtomicReference<Map<AuthType, ResolvedPrefix>> cache = new AtomicReference<>(Map.of());

    public PrefixRegistry(@NotNull PrefixFormatter formatter, @NotNull PluginConfiguration initial) {
        this.formatter = Objects.requireNonNull(formatter);
        this.configuration = new AtomicReference<>(Objects.requireNonNull(initial));
        rebuildCache(initial);
    }

    public void update(@NotNull PluginConfiguration configuration) {
        this.configuration.set(Objects.requireNonNull(configuration));
        rebuildCache(configuration);
    }

    public @NotNull PluginConfiguration configuration() {
        return configuration.get();
    }

    public @NotNull PrefixView view(@NotNull AuthType type) {
        return resolve(type).toView();
    }

    public @NotNull ResolvedPrefix resolve(@NotNull AuthType type) {
        PluginConfiguration config = configuration.get();
        if (config.settings().cachePrefixes()) {
            ResolvedPrefix cached = cache.get().get(type);
            if (cached != null) {
                return cached;
            }
        }
        return render(type, config);
    }

    public void invalidate() {
        rebuildCache(configuration.get());
    }

    private void rebuildCache(@NotNull PluginConfiguration config) {
        EnumMap<AuthType, ResolvedPrefix> next = new EnumMap<>(AuthType.class);
        for (AuthType type : AuthType.values()) {
            next.put(type, render(type, config));
        }
        cache.set(next);
    }

    private @NotNull ResolvedPrefix render(@NotNull AuthType type, @NotNull PluginConfiguration config) {
        PrefixDefinition definition = config.prefix(type);
        var component = formatter.render(definition.value(), config.formatting());
        return new ResolvedPrefix(
                type,
                definition.enabled() && !definition.value().isEmpty(),
                definition.value(),
                component,
                formatter.toLegacyAmpersand(component),
                formatter.toLegacySection(component)
        );
    }
}
