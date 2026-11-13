package com.premiumorcracked.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginLogger {
    void info(@NotNull String message);

    void warn(@NotNull String message);

    void error(@NotNull String message, @Nullable Throwable error);
}
