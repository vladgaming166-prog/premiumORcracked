package com.premiumorcracked.velocity;

import com.premiumorcracked.runtime.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

final class Slf4jPluginLogger implements PluginLogger {
    private final Logger logger;

    Slf4jPluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(@NotNull String message) {
        logger.info(message);
    }

    @Override
    public void warn(@NotNull String message) {
        logger.warn(message);
    }

    @Override
    public void error(@NotNull String message, @Nullable Throwable error) {
        if (error == null) {
            logger.error(message);
        } else {
            logger.error(message, error);
        }
    }
}
