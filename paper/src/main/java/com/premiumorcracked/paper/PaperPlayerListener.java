package com.premiumorcracked.paper;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.runtime.PrefixRuntime;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class PaperPlayerListener implements Listener {
    private final PremiumOrCrackedPaperPlugin plugin;
    private final PrefixRuntime runtime;
    private final PaperPrefixApplier applier;
    private final PaperAuthDetector detector;
    private final PaperPrefixSyncNetwork network;

    PaperPlayerListener(
            PremiumOrCrackedPaperPlugin plugin,
            PrefixRuntime runtime,
            PaperPrefixApplier applier,
            PaperAuthDetector detector,
            PaperPrefixSyncNetwork network
    ) {
        this.plugin = plugin;
        this.runtime = runtime;
        this.applier = applier;
        this.detector = detector;
        this.network = network;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!runtime.isVelocityLinked()) {
            AuthType type = detector.detect(player);
            runtime.store().put(player.getUniqueId(), player.getName(), type);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            applier.captureBaseline(player);
            applier.apply(player);
            network.requestSync(plugin.getServer().getName());
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        applier.forget(event.getPlayer());
        if (!runtime.isVelocityLinked()) {
            runtime.forget(event.getPlayer().getUniqueId());
        }
    }
}
