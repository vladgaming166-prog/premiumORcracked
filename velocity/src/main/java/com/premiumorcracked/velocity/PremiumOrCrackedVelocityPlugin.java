package com.premiumorcracked.velocity;

import com.google.inject.Inject;
import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PremiumOrCrackedProvider;
import com.premiumorcracked.config.ConfigurationException;
import com.premiumorcracked.config.ConfigurationLoader;
import com.premiumorcracked.config.ConfigurationValidator;
import com.premiumorcracked.prefix.PrefixFormatter;
import com.premiumorcracked.runtime.DefaultPremiumOrCrackedApi;
import com.premiumorcracked.runtime.PrefixRuntime;
import com.premiumorcracked.sync.ConfigSnapshot;
import com.premiumorcracked.sync.PlayerAuthRecord;
import com.premiumorcracked.sync.SyncChannels;
import com.premiumorcracked.sync.SyncCodec;
import com.premiumorcracked.sync.SyncMessage;
import com.premiumorcracked.sync.SyncProtocolException;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "premiumorcracked",
        name = "PremiumOrCracked",
        version = "1.0.0",
        description = "Configurable premium/cracked authentication prefixes",
        authors = {"Vlad Gaming"}
)
public final class PremiumOrCrackedVelocityPlugin {
    static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from(SyncChannels.CHANNEL);

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final VelocityAuthDetector detector = new VelocityAuthDetector();
    private final SyncCodec codec = new SyncCodec();
    private PrefixRuntime runtime;
    private VelocityPrefixSyncNetwork network;

    @Inject
    public PremiumOrCrackedVelocityPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            runtime = new PrefixRuntime(
                    ConfigurationLoader.configFile(dataDirectory),
                    new ConfigurationLoader(),
                    new ConfigurationValidator(),
                    new PrefixFormatter(),
                    new Slf4jPluginLogger(logger)
            );
        } catch (ConfigurationException ex) {
            logger.error("Failed to load config.yml. The prefix system will not start.", ex);
            return;
        }
        network = new VelocityPrefixSyncNetwork(proxy, CHANNEL, codec);
        runtime.setNetwork(network);
        PremiumOrCrackedProvider.register(new DefaultPremiumOrCrackedApi(runtime));

        proxy.getChannelRegistrar().register(CHANNEL);
        CommandManager commands = proxy.getCommandManager();
        commands.register(
                commands.metaBuilder("poc").aliases("premiumorcracked").plugin(this).build(),
                new PocVelocityCommand(runtime)
        );
        logger.info("PremiumOrCracked Velocity prefix authority is ready. Edit config.yml and run /poc reload.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        PremiumOrCrackedProvider.unregister(PremiumOrCrackedProvider.optional().orElse(null));
        proxy.getChannelRegistrar().unregister(CHANNEL);
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (runtime == null) {
            return;
        }
        Player player = event.getPlayer();
        AuthType type = detector.detect(player);
        runtime.rememberLocal(player.getUniqueId(), player.getUsername(), type);
        logger.info("{} authenticated as {}", player.getUsername(), type.name());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (runtime == null) {
            return;
        }
        Player player = event.getPlayer();
        AuthType type = runtime.store().typeOrUnknown(player.getUniqueId());
        if (runtime.store().get(player.getUniqueId()).isEmpty()) {
            type = detector.detect(player);
            runtime.rememberLocal(player.getUniqueId(), player.getUsername(), type);
        }
        byte[] config = codec.encode(new SyncMessage.ConfigSync(ConfigSnapshot.from(runtime.configuration())));
        byte[] playerPacket = codec.encode(new SyncMessage.PlayerSync(
                new PlayerAuthRecord(player.getUniqueId(), player.getUsername(), type)
        ));
        event.getServer().sendPluginMessage(CHANNEL, config);
        player.getCurrentServer().ifPresent(connection -> {
            connection.sendPluginMessage(CHANNEL, config);
            connection.sendPluginMessage(CHANNEL, playerPacket);
        });
        event.getServer().sendPluginMessage(CHANNEL, playerPacket);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (runtime != null) {
            runtime.forget(event.getPlayer().getUniqueId());
        }
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!CHANNEL.equals(event.getIdentifier()) || runtime == null) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof com.velocitypowered.api.proxy.ServerConnection serverConnection)) {
            return;
        }
        try {
            SyncMessage message = codec.decode(event.getData());
            if (message instanceof SyncMessage.RequestSync) {
                RegisteredServer server = serverConnection.getServer();
                server.sendPluginMessage(CHANNEL, codec.encode(new SyncMessage.ConfigSync(ConfigSnapshot.from(runtime.configuration()))));
                server.sendPluginMessage(CHANNEL, codec.encode(new SyncMessage.PlayerBulkSync(runtime.currentPlayerRecords())));
            }
        } catch (SyncProtocolException ex) {
            logger.warn("Ignored invalid sync packet from {}: {}", serverConnection.getServerInfo().getName(), ex.getMessage());
        }
    }
}
