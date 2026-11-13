package com.premiumorcracked.runtime;

import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;
import com.premiumorcracked.sync.PrefixSyncNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class PrefixSyncNetworks {
    static final PrefixSyncNetwork NO_OP = new PrefixSyncNetwork() {
        @Override
        public boolean broadcastConfig(@NotNull ConfigSnapshot snapshot) {
            return false;
        }

        @Override
        public boolean sendPlayer(@NotNull PlayerAuthRecord record) {
            return false;
        }

        @Override
        public boolean sendPlayers(@NotNull Collection<PlayerAuthRecord> records) {
            return false;
        }

        @Override
        public boolean requestSync(@NotNull String serverName) {
            return false;
        }
    };

    private PrefixSyncNetworks() {
    }
}
