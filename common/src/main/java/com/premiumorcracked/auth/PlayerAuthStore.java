package com.premiumorcracked.auth;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PlayerPrefixInfo;
import com.premiumorcracked.api.PrefixView;
import com.premiumorcracked.prefix.PrefixRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerAuthStore {
    private final Map<UUID, StoredPlayer> byId = new ConcurrentHashMap<>();
    private final Map<String, UUID> byName = new ConcurrentHashMap<>();

    public void put(@NotNull UUID uniqueId, @NotNull String username, @NotNull AuthType type) {
        StoredPlayer previous = byId.put(uniqueId, new StoredPlayer(uniqueId, username, type));
        if (previous != null) {
            byName.remove(normalize(previous.username()), uniqueId);
        }
        byName.put(normalize(username), uniqueId);
    }

    public void remove(@NotNull UUID uniqueId) {
        StoredPlayer removed = byId.remove(uniqueId);
        if (removed != null) {
            byName.remove(normalize(removed.username()), uniqueId);
        }
    }

    public void clear() {
        byId.clear();
        byName.clear();
    }

    public @NotNull Optional<StoredPlayer> get(@NotNull UUID uniqueId) {
        return Optional.ofNullable(byId.get(uniqueId));
    }

    public @NotNull Optional<StoredPlayer> getByName(@NotNull String username) {
        UUID id = byName.get(normalize(username));
        return id == null ? Optional.empty() : get(id);
    }

    public @NotNull AuthType typeOrUnknown(@NotNull UUID uniqueId) {
        StoredPlayer stored = byId.get(uniqueId);
        return stored == null ? AuthType.UNKNOWN : stored.type();
    }

    public @NotNull Collection<StoredPlayer> all() {
        return byId.values();
    }

    public @NotNull Optional<PlayerPrefixInfo> info(@NotNull UUID uniqueId, @NotNull PrefixRegistry registry) {
        StoredPlayer stored = byId.get(uniqueId);
        if (stored == null) {
            return Optional.empty();
        }
        PrefixView view = registry.view(stored.type());
        return Optional.of(new PlayerPrefixInfo(stored.uniqueId(), stored.username(), stored.type(), view));
    }

    public @NotNull PlayerPrefixInfo infoOrUnknown(
            @NotNull UUID uniqueId,
            @Nullable String username,
            @NotNull PrefixRegistry registry
    ) {
        StoredPlayer stored = byId.get(uniqueId);
        AuthType type = stored == null ? AuthType.UNKNOWN : stored.type();
        String name = stored != null ? stored.username() : (username == null ? uniqueId.toString() : username);
        return new PlayerPrefixInfo(uniqueId, name, type, registry.view(type));
    }

    public record StoredPlayer(@NotNull UUID uniqueId, @NotNull String username, @NotNull AuthType type) {
    }

    private static @NotNull String normalize(@NotNull String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
