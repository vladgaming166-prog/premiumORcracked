package com.premiumorcracked.velocity;

import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;
import com.premiumorcracked.sync.PrefixSyncNetwork;
import com.premiumorcracked.sync.SyncCodec;
import com.premiumorcracked.sync.SyncMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class VelocityPrefixSyncNetwork implements PrefixSyncNetwork {
    private final ProxyServer proxy;
    private final MinecraftChannelIdentifier channel;
    private final SyncCodec codec;

    VelocityPrefixSyncNetwork(ProxyServer proxy, MinecraftChannelIdentifier channel, SyncCodec codec) {
        this.proxy = proxy;
        this.channel = channel;
        this.codec = codec;
    }

    @Override
    public boolean broadcastConfig(@NotNull ConfigSnapshot snapshot) {
        return broadcast(codec.encode(new SyncMessage.ConfigSync(snapshot)));
    }

    @Override
    public boolean sendPlayer(@NotNull PlayerAuthRecord record) {
        byte[] payload = codec.encode(new SyncMessage.PlayerSync(record));
        Player player = proxy.getPlayer(record.uniqueId()).orElse(null);
        if (player != null) {
            return player.getCurrentServer()
                    .map(connection -> connection.sendPluginMessage(channel, payload))
                    .orElse(false);
        }
        return broadcast(payload);
    }

    @Override
    public boolean sendPlayers(@NotNull Collection<PlayerAuthRecord> records) {
        List<PlayerAuthRecord> list = List.copyOf(records);
        boolean sent = broadcast(codec.encode(new SyncMessage.PlayerBulkSync(list)));
        for (PlayerAuthRecord record : list) {
            sent |= sendPlayer(record);
        }
        return sent;
    }

    @Override
    public boolean requestSync(@NotNull String serverName) {
        return false;
    }

    private boolean broadcast(byte[] payload) {
        Set<String> sent = new HashSet<>();
        boolean any = false;
        for (Player player : proxy.getAllPlayers()) {
            var connection = player.getCurrentServer().orElse(null);
            if (connection == null) {
                continue;
            }
            String name = connection.getServerInfo().getName();
            if (sent.add(name)) {
                any |= connection.sendPluginMessage(channel, payload);
            }
        }
        for (RegisteredServer server : proxy.getAllServers()) {
            if (sent.contains(server.getServerInfo().getName())) {
                continue;
            }
            any |= server.sendPluginMessage(channel, payload);
        }
        return any;
    }
}
