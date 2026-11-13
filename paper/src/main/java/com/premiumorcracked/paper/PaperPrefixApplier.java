package com.premiumorcracked.paper;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PrefixView;
import com.premiumorcracked.config.DisplaySettings;
import com.premiumorcracked.display.DisplayComposer;
import com.premiumorcracked.runtime.PrefixRuntime;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies authentication prefixes independently of LuckPerms ranks.
 * Rank prefixes already present in the list name are preserved and never overwritten.
 */
final class PaperPrefixApplier {
    private final PrefixRuntime runtime;
    private final Map<UUID, Component> tabBaselines = new ConcurrentHashMap<>();
    private final Map<UUID, String> teamNames = new ConcurrentHashMap<>();

    PaperPrefixApplier(PrefixRuntime runtime) {
        this.runtime = runtime;
    }

    void captureBaseline(@NotNull Player player) {
        tabBaselines.putIfAbsent(player.getUniqueId(), player.playerListName());
    }

    void apply(@NotNull Player player) {
        captureBaseline(player);
        PrefixView prefix = runtime.prefix(player.getUniqueId());
        DisplaySettings display = runtime.configuration().display();
        applyTablist(player, prefix, display);
        applyTeams(player, prefix, display);
    }

    void apply(@NotNull UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null && player.isOnline()) {
            apply(player);
        }
    }

    void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            apply(player);
        }
    }

    void forget(@NotNull Player player) {
        restoreTablist(player);
        clearTeam(player);
        tabBaselines.remove(player.getUniqueId());
        teamNames.remove(player.getUniqueId());
    }

    void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            forget(player);
        }
        tabBaselines.clear();
        teamNames.clear();
    }

    private void applyTablist(@NotNull Player player, @NotNull PrefixView prefix, @NotNull DisplaySettings display) {
        Component baseline = tabBaselines.getOrDefault(player.getUniqueId(), player.playerListName());
        if (!display.tablist()) {
            player.playerListName(baseline);
            return;
        }
        player.playerListName(DisplayComposer.prepend(prefix.visibleComponent(), baseline));
    }

    private void restoreTablist(@NotNull Player player) {
        Component baseline = tabBaselines.get(player.getUniqueId());
        if (baseline != null) {
            player.playerListName(baseline);
        }
    }

    private void applyTeams(@NotNull Player player, @NotNull PrefixView prefix, @NotNull DisplaySettings display) {
        if (!display.nametag() && !display.scoreboard()) {
            clearTeam(player);
            return;
        }
        Scoreboard scoreboard = player.getScoreboard();
        String name = teamName(player.getUniqueId(), runtime.store().typeOrUnknown(player.getUniqueId()));
        String previous = teamNames.put(player.getUniqueId(), name);
        if (previous != null && !previous.equals(name)) {
            Team old = scoreboard.getTeam(previous);
            if (old != null) {
                old.removeEntry(player.getName());
                if (old.getEntries().isEmpty()) {
                    old.unregister();
                }
            }
        }
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }
        team.prefix(prefix.visibleComponent());
        team.suffix(Component.empty());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY,
                display.nametag() ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    private void clearTeam(@NotNull Player player) {
        String name = teamNames.remove(player.getUniqueId());
        if (name == null) {
            return;
        }
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            return;
        }
        team.removeEntry(player.getName());
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }

    static @NotNull String teamName(@NotNull UUID uniqueId, @NotNull AuthType type) {
        char sort = switch (type) {
            case PREMIUM -> '0';
            case CRACKED -> '1';
            case UNKNOWN -> '2';
        };
        String compact = uniqueId.toString().replace("-", "");
        return (sort + compact).substring(0, 16);
    }
}
