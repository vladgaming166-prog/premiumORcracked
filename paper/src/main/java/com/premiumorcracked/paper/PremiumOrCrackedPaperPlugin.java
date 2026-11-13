package com.premiumorcracked.paper;

import com.premiumorcracked.api.PremiumOrCrackedApi;
import com.premiumorcracked.api.PremiumOrCrackedProvider;
import com.premiumorcracked.config.ConfigurationException;
import com.premiumorcracked.config.ConfigurationLoader;
import com.premiumorcracked.config.ConfigurationValidator;
import com.premiumorcracked.prefix.PrefixFormatter;
import com.premiumorcracked.runtime.DefaultPremiumOrCrackedApi;
import com.premiumorcracked.runtime.PrefixRuntime;
import com.premiumorcracked.sync.SyncChannels;
import com.premiumorcracked.sync.SyncCodec;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class PremiumOrCrackedPaperPlugin extends JavaPlugin {
    private PrefixRuntime runtime;
    private PaperPrefixApplier applier;
    private DefaultPremiumOrCrackedApi api;
    private PremiumOrCrackedExpansion expansion;

    @Override
    public void onEnable() {
        saveDefaultConfigResource();
        try {
            runtime = new PrefixRuntime(
                    ConfigurationLoader.configFile(getDataFolder().toPath()),
                    new ConfigurationLoader(),
                    new ConfigurationValidator(),
                    new PrefixFormatter(),
                    new JavaPluginLogger(getLogger())
            );
        } catch (ConfigurationException ex) {
            getLogger().severe("Failed to load config.yml: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SyncCodec codec = new SyncCodec();
        PaperPrefixSyncNetwork network = new PaperPrefixSyncNetwork(this, codec);
        runtime.setNetwork(network);
        applier = new PaperPrefixApplier(runtime);
        api = new DefaultPremiumOrCrackedApi(runtime);
        PremiumOrCrackedProvider.register(api);
        getServer().getServicesManager().register(PremiumOrCrackedApi.class, api, this, ServicePriority.Normal);

        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                SyncChannels.CHANNEL,
                new PaperSyncListener(this, runtime, codec, applier)
        );
        getServer().getMessenger().registerOutgoingPluginChannel(this, SyncChannels.CHANNEL);

        PaperAuthDetector detector = new PaperAuthDetector();
        getServer().getPluginManager().registerEvents(new PaperPlayerListener(this, runtime, applier, detector, network), this);
        getServer().getPluginManager().registerEvents(new PaperChatListener(runtime), this);

        PocPaperCommand command = new PocPaperCommand(this, runtime, applier, network);
        var poc = getCommand("poc");
        if (poc != null) {
            poc.setExecutor(command);
            poc.setTabCompleter(command);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new PremiumOrCrackedExpansion(this, api);
            expansion.register();
            getLogger().info("Hooked PlaceholderAPI placeholders %premiumorcracked_prefix%, %premiumorcracked_type%, %premiumorcracked_display%.");
        }

        if (runtime.configuration().settings().updatePrefixWithoutRestart()) {
            getServer().getScheduler().runTaskTimer(this, applier::refreshAll, 100L, 100L);
        }

        runtime.addListener(configuration -> getServer().getScheduler().runTask(this, applier::refreshAll));
        getLogger().info("PremiumOrCracked Paper companion is ready. Velocity is the authentication prefix authority.");
    }

    @Override
    public void onDisable() {
        if (expansion != null) {
            expansion.unregister();
        }
        if (applier != null) {
            applier.shutdown();
        }
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getServicesManager().unregisterAll(this);
        PremiumOrCrackedProvider.unregister(api);
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void saveDefaultConfigResource() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder");
        }
        saveResource("config.yml", false);
    }
}
