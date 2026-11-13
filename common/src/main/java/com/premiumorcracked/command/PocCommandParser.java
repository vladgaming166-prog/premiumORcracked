package com.premiumorcracked.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class PocCommandParser {
    public enum Action {
        RELOAD,
        STATUS,
        HELP,
        UNKNOWN
    }

    private PocCommandParser() {
    }

    public static @NotNull Action parse(@Nullable String[] args) {
        if (args == null || args.length == 0) {
            return Action.HELP;
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> Action.RELOAD;
            case "status" -> Action.STATUS;
            case "help" -> Action.HELP;
            default -> Action.UNKNOWN;
        };
    }

    public static @NotNull List<String> suggestions(@Nullable String[] args) {
        List<String> options = List.of("reload", "status", "help");
        if (args == null || args.length == 0) {
            return options;
        }
        if (args.length > 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(prefix)).toList();
    }
}
