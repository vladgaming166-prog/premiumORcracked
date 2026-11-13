package com.premiumorcracked.paper;

import com.premiumorcracked.runtime.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

final class JavaPluginLogger implements PluginLogger {
    private final Logger logger;

    JavaPluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(@NotNull String message) {
        logger.info(message);
    }

    @Override
    public void warn(@NotNull String message) {
        logger.warning(message);
    }

    @Override
    public void error(@NotNull String message, @Nullable Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }
}
