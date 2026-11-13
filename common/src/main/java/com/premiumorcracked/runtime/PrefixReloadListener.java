package com.premiumorcracked.runtime;

import com.premiumorcracked.config.PluginConfiguration;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PrefixReloadListener {
    void onReloaded(@NotNull PluginConfiguration configuration);
}
