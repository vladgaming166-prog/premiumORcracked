package com.premiumorcracked.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class ListPluginLogger implements PluginLogger {
    final List<String> messages = new ArrayList<>();

    @Override
    public void info(@NotNull String message) {
        messages.add("INFO " + message);
    }

    @Override
    public void warn(@NotNull String message) {
        messages.add("WARN " + message);
    }

    @Override
    public void error(@NotNull String message, @Nullable Throwable error) {
        messages.add("ERROR " + message);
    }
}
