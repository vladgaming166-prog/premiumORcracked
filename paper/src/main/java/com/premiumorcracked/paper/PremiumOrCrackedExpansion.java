package com.premiumorcracked.paper;

import com.premiumorcracked.api.PremiumOrCrackedApi;
import com.premiumorcracked.placeholder.PrefixPlaceholders;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PremiumOrCrackedExpansion extends PlaceholderExpansion {
    private final PremiumOrCrackedPaperPlugin plugin;
    private final PremiumOrCrackedApi api;

    PremiumOrCrackedExpansion(PremiumOrCrackedPaperPlugin plugin, PremiumOrCrackedApi api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public @NotNull String getIdentifier() {
        return PrefixPlaceholders.IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        String name = player.getName();
        String resolved = PrefixPlaceholders.resolve(params, api, player.getUniqueId(), name);
        return resolved == null ? null : resolved;
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        return onRequest(player, params);
    }
}
