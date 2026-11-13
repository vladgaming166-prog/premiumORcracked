package com.premiumorcracked.runtime;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PlayerPrefixInfo;
import com.premiumorcracked.api.PrefixView;
import com.premiumorcracked.auth.PlayerAuthStore;
import com.premiumorcracked.config.ConfigurationException;
import com.premiumorcracked.config.ConfigurationLoader;
import com.premiumorcracked.config.ConfigurationValidator;
import com.premiumorcracked.config.PluginConfiguration;
import com.premiumorcracked.config.ValidationResult;
import com.premiumorcracked.prefix.PrefixFormatter;
import com.premiumorcracked.prefix.PrefixRegistry;
import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;
import com.premiumorcracked.sync.PrefixSyncNetwork;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Shared reload / cache / sync orchestration used by Velocity and Paper.
 */
public final class PrefixRuntime {
    private final Path configFile;
    private final ConfigurationLoader loader;
    private final ConfigurationValidator validator;
    private final PrefixRegistry registry;
    private final PlayerAuthStore store;
    private final PluginLogger logger;
    private final List<PrefixReloadListener> listeners = new CopyOnWriteArrayList<>();
    private volatile PrefixSyncNetwork network = PrefixSyncNetworks.NO_OP;
    private volatile boolean velocityLinked;

    public PrefixRuntime(
            @NotNull Path configFile,
            @NotNull ConfigurationLoader loader,
            @NotNull ConfigurationValidator validator,
            @NotNull PrefixFormatter formatter,
            @NotNull PluginLogger logger
    ) throws ConfigurationException {
        this.configFile = Objects.requireNonNull(configFile);
        this.loader = loader;
        this.validator = validator;
        this.logger = logger;
        PluginConfiguration loaded = loader.load(configFile);
        ValidationResult validation = validator.validate(loaded);
        if (!validation.success()) {
            logger.warn("config.yml has issues (" + validation.formattedErrors() + "); using safe defaults until it is fixed.");
            loaded = PluginConfiguration.defaults();
        }
        this.registry = new PrefixRegistry(formatter, loaded);
        this.store = new PlayerAuthStore();
    }

    public void setNetwork(@Nullable PrefixSyncNetwork network) {
        this.network = network == null ? PrefixSyncNetworks.NO_OP : network;
    }

    public void addListener(@NotNull PrefixReloadListener listener) {
        listeners.add(listener);
    }

    public @NotNull PrefixRegistry registry() {
        return registry;
    }

    public @NotNull PlayerAuthStore store() {
        return store;
    }

    public @NotNull PluginConfiguration configuration() {
        return registry.configuration();
    }

    public boolean isVelocityLinked() {
        return velocityLinked;
    }

    public @NotNull ReloadResult reloadFromDisk() {
        try {
            PluginConfiguration loaded = loader.load(configFile);
            return applyConfiguration(loaded, true);
        } catch (ConfigurationException ex) {
            logger.error("Failed to reload config.yml", ex);
            return ReloadResult.failure(List.of(ex.getMessage()));
        }
    }

    public @NotNull ReloadResult applyConfiguration(@NotNull PluginConfiguration loaded, boolean syncBackends) {
        ValidationResult validation = validator.validate(loaded);
        if (!validation.success()) {
            logger.warn("Rejected invalid prefix configuration: " + validation.formattedErrors());
            return ReloadResult.failure(validation.errors());
        }
        registry.update(loaded);
        int backends = 0;
        if (syncBackends) {
            backends = broadcastCurrentState();
        }
        for (PrefixReloadListener listener : listeners) {
            try {
                listener.onReloaded(loaded);
            } catch (RuntimeException ex) {
                logger.error("Prefix reload listener failed", ex);
            }
        }
        logger.info("Loaded prefixes: premium='" + loaded.prefix(AuthType.PREMIUM).value()
                + "' cracked='" + loaded.prefix(AuthType.CRACKED).value()
                + "' unknown='" + loaded.prefix(AuthType.UNKNOWN).value() + "'");
        return ReloadResult.success(loaded, backends);
    }

    public int broadcastCurrentState() {
        boolean configSent = network.broadcastConfig(ConfigSnapshot.from(configuration()));
        List<PlayerAuthRecord> records = currentPlayerRecords();
        boolean playersSent = records.isEmpty() || network.sendPlayers(records);
        return (configSent ? 1 : 0) + (playersSent ? records.size() : 0);
    }

    public void applyRemoteSnapshot(@NotNull ConfigSnapshot snapshot) {
        velocityLinked = true;
        PluginConfiguration incoming = snapshot.toConfiguration();
        ValidationResult validation = validator.validate(incoming);
        if (!validation.success()) {
            logger.warn("Ignored invalid prefix snapshot from Velocity: " + validation.formattedErrors());
            return;
        }
        registry.update(incoming);
        for (PrefixReloadListener listener : listeners) {
            listener.onReloaded(incoming);
        }
        logger.info("Applied synchronized prefix configuration from Velocity.");
    }

    public void applyRemotePlayer(@NotNull PlayerAuthRecord record) {
        velocityLinked = true;
        store.put(record.uniqueId(), record.username(), record.type());
    }

    public void rememberLocal(@NotNull UUID uniqueId, @NotNull String username, @NotNull AuthType type) {
        store.put(uniqueId, username, type);
        network.sendPlayer(new PlayerAuthRecord(uniqueId, username, type));
    }

    public void forget(@NotNull UUID uniqueId) {
        store.remove(uniqueId);
    }

    public @NotNull PrefixView prefix(@NotNull AuthType type) {
        return registry.view(type);
    }

    public @NotNull PrefixView prefix(@NotNull UUID playerId) {
        return registry.view(store.typeOrUnknown(playerId));
    }

    public @NotNull PlayerPrefixInfo info(@NotNull UUID uniqueId, @Nullable String username) {
        return store.infoOrUnknown(uniqueId, username, registry);
    }

    public @NotNull List<PlayerAuthRecord> currentPlayerRecords() {
        List<PlayerAuthRecord> records = new ArrayList<>();
        store.all().forEach(player -> records.add(new PlayerAuthRecord(player.uniqueId(), player.username(), player.type())));
        return records;
    }

    public void requestBackendSync(@NotNull String serverName) {
        network.requestSync(serverName);
    }
}
