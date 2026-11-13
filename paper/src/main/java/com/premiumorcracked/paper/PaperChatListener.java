package com.premiumorcracked.paper;

import com.premiumorcracked.runtime.PrefixRuntime;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Prepends the authentication prefix to chat without replacing LuckPerms or other chat formats.
 */
final class PaperChatListener implements Listener {
    private final PrefixRuntime runtime;

    PaperChatListener(PrefixRuntime runtime) {
        this.runtime = runtime;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!runtime.configuration().display().chat()) {
            return;
        }
        Component prefix = runtime.prefix(event.getPlayer().getUniqueId()).visibleComponent();
        if (Component.empty().equals(prefix)) {
            return;
        }
        ChatRenderer original = event.renderer();
        event.renderer((source, sourceDisplayName, message, viewer) ->
                Component.empty().append(prefix).append(original.render(source, sourceDisplayName, message, viewer)));
    }
}
