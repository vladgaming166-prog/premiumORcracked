package com.premiumorcracked.paper;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.auth.AuthTypeDetector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class PaperAuthDetector {
    private final AuthTypeDetector detector = new AuthTypeDetector();

    @NotNull AuthType detect(@NotNull Player player) {
        boolean sessionLooksPremium = Bukkit.getOnlineMode()
                || (player.getUniqueId().version() == 4 && !player.getPlayerProfile().getTextures().isEmpty());
        return detector.detect(sessionLooksPremium, player.getUniqueId(), player.getName());
    }
}
