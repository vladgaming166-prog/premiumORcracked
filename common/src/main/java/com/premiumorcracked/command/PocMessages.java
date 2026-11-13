package com.premiumorcracked.command;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.config.PluginConfiguration;
import com.premiumorcracked.config.PrefixDefinition;
import com.premiumorcracked.runtime.ReloadResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public final class PocMessages {
    private PocMessages() {
    }

    public static @NotNull Component prefix() {
        return Component.text("[POC] ", NamedTextColor.GRAY);
    }

    public static @NotNull Component help() {
        return prefix().append(Component.text("Commands: /poc reload, /poc status, /poc help", NamedTextColor.WHITE));
    }

    public static @NotNull Component unknown() {
        return prefix().append(Component.text("Unknown subcommand. Use /poc help.", NamedTextColor.RED));
    }

    public static @NotNull Component noPermission() {
        return prefix().append(Component.text("You do not have permission to do that.", NamedTextColor.RED));
    }

    public static @NotNull Component reloadSuccess(@NotNull ReloadResult result) {
        return prefix().append(Component.text(result.message(), NamedTextColor.GREEN));
    }

    public static @NotNull Component reloadFailure(@NotNull ReloadResult result) {
        return prefix().append(Component.text(result.message(), NamedTextColor.RED));
    }

    public static @NotNull Component status(@NotNull PluginConfiguration configuration, boolean velocityLinked) {
        String source = velocityLinked ? "Velocity (synchronized)" : "local config.yml";
        Component header = prefix().append(Component.text("Prefix source: " + source, NamedTextColor.AQUA));
        Component body = Component.empty();
        for (AuthType type : AuthType.values()) {
            PrefixDefinition definition = configuration.prefix(type);
            body = body.append(Component.newline())
                    .append(Component.text("  " + type.key() + ": ", NamedTextColor.GRAY))
                    .append(Component.text(definition.enabled() ? "enabled " : "disabled ", definition.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
                    .append(Component.text(quote(definition.value()), NamedTextColor.WHITE));
        }
        return header.append(body);
    }

    private static @NotNull String quote(@NotNull String value) {
        return value.isEmpty() ? "(empty)" : "'" + value + "'";
    }
}
