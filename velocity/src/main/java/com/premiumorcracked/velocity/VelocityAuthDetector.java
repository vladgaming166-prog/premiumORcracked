package com.premiumorcracked.velocity;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.auth.AuthTypeDetector;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

final class VelocityAuthDetector {
    private final AuthTypeDetector detector = new AuthTypeDetector();

    @NotNull AuthType detect(@NotNull Player player) {
        return detector.detect(player.isOnlineMode(), player.getUniqueId(), player.getUsername());
    }
}
