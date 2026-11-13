package com.premiumorcracked.runtime;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.config.PluginConfiguration;
import com.premiumorcracked.prefix.PrefixFormatter;
import com.premiumorcracked.prefix.PrefixRegistry;
import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class FakeBackend {
    final String name;
    final PrefixRegistry registry = new PrefixRegistry(new PrefixFormatter(), PluginConfiguration.defaults());
    final Map<UUID, AuthType> types = new ConcurrentHashMap<>();

    FakeBackend(String name) {
        this.name = name;
    }

    void apply(ConfigSnapshot snapshot) {
        registry.update(snapshot.toConfiguration());
    }

    void apply(PlayerAuthRecord record) {
        types.put(record.uniqueId(), record.type());
    }

    String prefix(AuthType type) {
        return registry.view(type).rawValue();
    }

    String prefixFor(UUID playerId) {
        AuthType type = types.getOrDefault(playerId, AuthType.UNKNOWN);
        return registry.view(type).visibleLegacyAmpersand();
    }
}
