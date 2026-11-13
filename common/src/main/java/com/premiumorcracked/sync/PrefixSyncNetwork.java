package com.premiumorcracked.sync;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Sends prefix configuration and player authentication types from Velocity to Paper.
 */
public interface PrefixSyncNetwork {
    boolean broadcastConfig(@NotNull ConfigSnapshot snapshot);

    boolean sendPlayer(@NotNull PlayerAuthRecord record);

    boolean sendPlayers(@NotNull Collection<PlayerAuthRecord> records);

    boolean requestSync(@NotNull String serverName);
}
