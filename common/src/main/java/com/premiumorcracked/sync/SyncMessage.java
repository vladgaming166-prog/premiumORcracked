package com.premiumorcracked.sync;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface SyncMessage {
    byte CONFIG = 1;
    byte PLAYER = 2;
    byte PLAYER_BULK = 3;
    byte REQUEST_SYNC = 4;

    byte id();

    record ConfigSync(@NotNull ConfigSnapshot snapshot) implements SyncMessage {
        @Override
        public byte id() {
            return CONFIG;
        }
    }

    record PlayerSync(@NotNull PlayerAuthRecord record) implements SyncMessage {
        @Override
        public byte id() {
            return PLAYER;
        }
    }

    record PlayerBulkSync(@NotNull List<PlayerAuthRecord> records) implements SyncMessage {
        @Override
        public byte id() {
            return PLAYER_BULK;
        }
    }

    record RequestSync(@NotNull String serverName) implements SyncMessage {
        @Override
        public byte id() {
            return REQUEST_SYNC;
        }
    }
}
