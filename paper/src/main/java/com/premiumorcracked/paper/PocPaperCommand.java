package com.premiumorcracked.paper;

import com.premiumorcracked.command.PocCommandParser;
import com.premiumorcracked.command.PocMessages;
import com.premiumorcracked.runtime.PrefixRuntime;
import com.premiumorcracked.runtime.ReloadResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class PocPaperCommand implements CommandExecutor, TabCompleter {
    private final PremiumOrCrackedPaperPlugin plugin;
    private final PrefixRuntime runtime;
    private final PaperPrefixApplier applier;
    private final PaperPrefixSyncNetwork network;

    PocPaperCommand(
            PremiumOrCrackedPaperPlugin plugin,
            PrefixRuntime runtime,
            PaperPrefixApplier applier,
            PaperPrefixSyncNetwork network
    ) {
        this.plugin = plugin;
        this.runtime = runtime;
        this.applier = applier;
        this.network = network;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        PocCommandParser.Action action = PocCommandParser.parse(args);
        switch (action) {
            case HELP -> sender.sendMessage(PocMessages.help());
            case UNKNOWN -> sender.sendMessage(PocMessages.unknown());
            case STATUS -> sender.sendMessage(PocMessages.status(runtime.configuration(), runtime.isVelocityLinked()));
            case RELOAD -> {
                if (!sender.hasPermission("premiumorcracked.reload")) {
                    sender.sendMessage(PocMessages.noPermission());
                    return true;
                }
                if (runtime.isVelocityLinked()) {
                    boolean requested = network.requestSync(plugin.getServer().getName());
                    if (requested) {
                        sender.sendMessage(PocMessages.prefix().append(Component.text(
                                "Requested a live prefix refresh from Velocity. Prefixes will update without restart.",
                                NamedTextColor.GREEN
                        )));
                    } else {
                        sender.sendMessage(PocMessages.reloadFailure(ReloadResult.failure(List.of(
                                "No player is online to request a Velocity prefix sync. Ask an administrator to run /poc reload on Velocity."
                        ))));
                    }
                    return true;
                }
                ReloadResult result = runtime.reloadFromDisk();
                if (result.success()) {
                    applier.refreshAll();
                    sender.sendMessage(PocMessages.reloadSuccess(result));
                } else {
                    sender.sendMessage(PocMessages.reloadFailure(result));
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return PocCommandParser.suggestions(args);
    }
}
