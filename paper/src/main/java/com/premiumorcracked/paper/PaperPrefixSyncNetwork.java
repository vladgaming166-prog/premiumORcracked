package com.premiumorcracked.paper;

import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;
import com.premiumorcracked.sync.PrefixSyncNetwork;
import com.premiumorcracked.sync.SyncChannels;
import com.premiumorcracked.sync.SyncCodec;
import com.premiumorcracked.sync.SyncMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class PaperPrefixSyncNetwork implements PrefixSyncNetwork {
    private final JavaPlugin plugin;
    private final SyncCodec codec;

    PaperPrefixSyncNetwork(JavaPlugin plugin, SyncCodec codec) {
        this.plugin = plugin;
        this.codec = codec;
    }

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
        byte[] payload = codec.encode(new SyncMessage.RequestSync(serverName));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendPluginMessage(plugin, SyncChannels.CHANNEL, payload);
            return true;
        }
        return false;
    }
}
