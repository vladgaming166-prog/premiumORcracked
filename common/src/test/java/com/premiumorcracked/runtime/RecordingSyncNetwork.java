package com.premiumorcracked.runtime;

import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;
import com.premiumorcracked.sync.PrefixSyncNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class RecordingSyncNetwork implements PrefixSyncNetwork {
    final List<ConfigSnapshot> configs = new CopyOnWriteArrayList<>();
    final List<PlayerAuthRecord> players = new CopyOnWriteArrayList<>();
    final List<String> requests = new CopyOnWriteArrayList<>();

    @Override
    public boolean broadcastConfig(@NotNull ConfigSnapshot snapshot) {
        configs.add(snapshot);
        return true;
    }

    @Override
    public boolean sendPlayer(@NotNull PlayerAuthRecord record) {
        players.add(record);
        return true;
    }

    @Override
    public boolean sendPlayers(@NotNull Collection<PlayerAuthRecord> records) {
        players.addAll(records);
        return true;
    }

    @Override
    public boolean requestSync(@NotNull String serverName) {
        requests.add(serverName);
        return true;
    }

    List<FakeBackend> fanOut(String... names) {
        List<FakeBackend> backends = new ArrayList<>();
        ConfigSnapshot latest = configs.isEmpty() ? null : configs.get(configs.size() - 1);
        for (String name : names) {
            FakeBackend backend = new FakeBackend(name);
            if (latest != null) {
                backend.apply(latest);
            }
            players.forEach(backend::apply);
            backends.add(backend);
        }
        return backends;
    }
}
