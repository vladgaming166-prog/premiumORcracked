package com.premiumorcracked.runtime;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PremiumOrCrackedApi;
import com.premiumorcracked.config.ConfigurationLoader;
import com.premiumorcracked.config.ConfigurationValidator;
import com.premiumorcracked.placeholder.PrefixPlaceholders;
import com.premiumorcracked.prefix.PrefixFormatter;
import com.premiumorcracked.sync.ConfigSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixRuntimeTest {
    @TempDir
    Path dir;

    private PrefixRuntime runtime;
    private RecordingSyncNetwork network;
    private Path configFile;

    @BeforeEach
    void setUp() throws Exception {
        configFile = dir.resolve("config.yml");
        runtime = new PrefixRuntime(
                configFile,
                new ConfigurationLoader(),
                new ConfigurationValidator(),
                new PrefixFormatter(),
                new ListPluginLogger()
        );
        network = new RecordingSyncNetwork();
        runtime.setNetwork(network);
    }

    @Test
    void reloadReadsNewPrefixesValidatesCachesAndSyncsBackends() throws Exception {
        UUID steve = UUID.randomUUID();
        runtime.rememberLocal(steve, "Steve", AuthType.PREMIUM);

        Files.writeString(configFile, """
                prefixes:
                  premium:
                    enabled: true
                    value: "&7[&6PREM&7] "
                  cracked:
                    enabled: true
                    value: "&7[&fCRACK&7] "
                  unknown:
                    enabled: true
                    value: "&7[&8?&7] "
                """, StandardCharsets.UTF_8);

        ReloadResult result = runtime.reloadFromDisk();
        assertThat(result.success()).isTrue();
        assertThat(runtime.prefix(AuthType.PREMIUM).rawValue()).isEqualTo("&7[&6PREM&7] ");
        assertThat(network.configs).isNotEmpty();
        assertThat(network.configs.getLast().prefixes.get("premium").value).isEqualTo("&7[&6PREM&7] ");

        FakeBackend lobby = new FakeBackend("lobby");
        FakeBackend survival = new FakeBackend("survival");
        lobby.apply(network.configs.getLast());
        survival.apply(network.configs.getLast());
        network.players.forEach(lobby::apply);
        network.players.forEach(survival::apply);

        assertThat(lobby.prefix(AuthType.PREMIUM)).isEqualTo(survival.prefix(AuthType.PREMIUM));
        assertThat(lobby.prefixFor(steve)).isEqualTo(survival.prefixFor(steve));
        assertThat(lobby.prefixFor(steve)).contains("PREM");
    }

    @Test
    void invalidReloadKeepsPreviousPrefixes() throws Exception {
        String original = runtime.prefix(AuthType.PREMIUM).rawValue();
        Files.writeString(configFile, """
                prefixes:
                  premium:
                    enabled: true
                    value: "<red>UNCLOSED"
                """, StandardCharsets.UTF_8);

        ReloadResult result = runtime.reloadFromDisk();
        assertThat(result.success()).isFalse();
        assertThat(runtime.prefix(AuthType.PREMIUM).rawValue()).isEqualTo(original);
        assertThat(network.configs).isEmpty();
    }

    @Test
    void remoteSnapshotMakesEveryBackendUseVelocityValues() {
        ConfigSnapshot snapshot;
        try {
            Files.writeString(configFile, """
                    prefixes:
                      premium:
                        enabled: true
                        value: "<gray>[<green>PREMIUM</green>]</gray> "
                    """, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        ReloadResult result = runtime.reloadFromDisk();
        assertThat(result.success()).isTrue();
        snapshot = network.configs.getLast();

        PrefixRuntime paperLobby = paperRuntime();
        PrefixRuntime paperSurvival = paperRuntime();
        paperLobby.applyRemoteSnapshot(snapshot);
        paperSurvival.applyRemoteSnapshot(snapshot);

        assertThat(paperLobby.prefix(AuthType.PREMIUM).rawValue())
                .isEqualTo(paperSurvival.prefix(AuthType.PREMIUM).rawValue())
                .isEqualTo("<gray>[<green>PREMIUM</green>]</gray> ");
        assertThat(paperLobby.isVelocityLinked()).isTrue();
    }

    @Test
    void apiAndPlaceholdersExposeIndependentPrefix() {
        UUID id = UUID.randomUUID();
        runtime.rememberLocal(id, "Alex", AuthType.CRACKED);
        PremiumOrCrackedApi api = new DefaultPremiumOrCrackedApi(runtime);

        assertThat(api.getTypeName(id)).isEqualTo("CRACKED");
        assertThat(api.getPrefixLegacy(id)).isNotBlank();
        assertThat(api.prependToExisting(id, "&c[Admin] Alex")).startsWith(api.getPrefixLegacy(id));
        assertThat(api.prependToExisting(id, "&c[Admin] Alex")).contains("[Admin]");
        assertThat(api.prependToExisting(id, "&c[Admin] Alex")).isNotEqualTo(api.getPrefixLegacy(id) + "Alex");

        assertThat(PrefixPlaceholders.resolve("prefix", api, id, "Alex")).isEqualTo(api.getPrefixLegacy(id));
        assertThat(PrefixPlaceholders.resolve("type", api, id, "Alex")).isEqualTo("CRACKED");
        assertThat(PrefixPlaceholders.resolve("display", api, id, "Alex")).isEqualTo(api.getPrefixLegacy(id) + "Alex");
    }

    private PrefixRuntime paperRuntime() {
        try {
            Path paperDir = Files.createTempDirectory(dir, "paper");
            return new PrefixRuntime(
                    paperDir.resolve("config.yml"),
                    new ConfigurationLoader(),
                    new ConfigurationValidator(),
                    new PrefixFormatter(),
                    new ListPluginLogger()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
