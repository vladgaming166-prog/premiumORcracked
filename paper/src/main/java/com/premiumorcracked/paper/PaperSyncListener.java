package com.premiumorcracked.paper;

import com.premiumorcracked.runtime.PrefixRuntime;
import com.premiumorcracked.sync.SyncChannels;
import com.premiumorcracked.sync.SyncCodec;
import com.premiumorcracked.sync.SyncMessage;
import com.premiumorcracked.sync.SyncProtocolException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

final class PaperSyncListener implements PluginMessageListener {
    private final PremiumOrCrackedPaperPlugin plugin;
    private final PrefixRuntime runtime;
    private final SyncCodec codec;
    private final PaperPrefixApplier applier;

    PaperSyncListener(
            PremiumOrCrackedPaperPlugin plugin,
            PrefixRuntime runtime,
            SyncCodec codec,
            PaperPrefixApplier applier
    ) {
        this.plugin = plugin;
        this.runtime = runtime;
        this.codec = codec;
        this.applier = applier;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!SyncChannels.CHANNEL.equals(channel)) {
            return;
        }
        try {
            switch (codec.decode(message)) {
                case SyncMessage.ConfigSync configSync -> {
                    runtime.applyRemoteSnapshot(configSync.snapshot());
                    applier.refreshAll();
                }
                case SyncMessage.PlayerSync playerSync -> {
                    runtime.applyRemotePlayer(playerSync.record());
                    applier.apply(playerSync.record().uniqueId());
                }
                case SyncMessage.PlayerBulkSync bulkSync -> {
                    bulkSync.records().forEach(runtime::applyRemotePlayer);
                    applier.refreshAll();
                }
                case SyncMessage.RequestSync ignored -> {
                    // Paper never answers requests; Velocity is the authority.
                }
            }
        } catch (SyncProtocolException ex) {
            plugin.getLogger().warning("Ignored invalid Velocity sync packet: " + ex.getMessage());
        }
    }
}
