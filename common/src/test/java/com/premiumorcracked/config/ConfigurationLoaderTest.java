package com.premiumorcracked.config;

import com.premiumorcracked.api.AuthType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationLoaderTest {
    private final ConfigurationLoader loader = new ConfigurationLoader();

    @Test
    void bundledDefaultMatchesRequiredSchema() throws Exception {
        PluginConfiguration config = loader.loadDefaultResource();
        assertThat(config.prefix(AuthType.PREMIUM).enabled()).isTrue();
        assertThat(config.prefix(AuthType.PREMIUM).value()).isEqualTo("&7[&aP&7] ");
        assertThat(config.prefix(AuthType.CRACKED).enabled()).isTrue();
        assertThat(config.prefix(AuthType.CRACKED).value()).isEqualTo("&7[&fC&7] ");
        assertThat(config.prefix(AuthType.UNKNOWN).enabled()).isTrue();
        assertThat(config.prefix(AuthType.UNKNOWN).value()).isEqualTo("&7[&8?&7] ");
        assertThat(config.display().tablist()).isTrue();
        assertThat(config.display().chat()).isFalse();
        assertThat(config.display().nametag()).isFalse();
        assertThat(config.display().scoreboard()).isFalse();
        assertThat(config.formatting().colorSupport()).isTrue();
        assertThat(config.formatting().legacyColors()).isTrue();
        assertThat(config.formatting().miniMessage()).isTrue();
        assertThat(config.settings().updatePrefixWithoutRestart()).isTrue();
        assertThat(config.settings().cachePrefixes()).isTrue();
    }

    @Test
    void customLegacyAndMiniMessagePrefixesLoadWithoutRebuild(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config.yml");
        Files.writeString(file, """
                prefixes:
                  premium:
                    enabled: true
                    value: "&7[&6PREM&7] "
                  cracked:
                    enabled: true
                    value: "<gray>[<white>CRACKED</white>]</gray> "
                  unknown:
                    enabled: false
                    value: ""
                """, StandardCharsets.UTF_8);

        PluginConfiguration config = loader.load(file);
        assertThat(config.prefix(AuthType.PREMIUM).value()).isEqualTo("&7[&6PREM&7] ");
        assertThat(config.prefix(AuthType.CRACKED).value()).isEqualTo("<gray>[<white>CRACKED</white>]</gray> ");
        assertThat(config.prefix(AuthType.UNKNOWN).enabled()).isFalse();
        assertThat(config.prefix(AuthType.UNKNOWN).value()).isEmpty();
    }

    @Test
    void missingKeysFallBackToDefaults() throws Exception {
        PluginConfiguration config = loader.load(new StringReader("settings:\n  cache-prefixes: false\n"));
        assertThat(config.prefix(AuthType.PREMIUM).value()).isEqualTo("&7[&aP&7] ");
        assertThat(config.settings().cachePrefixes()).isFalse();
        assertThat(config.settings().updatePrefixWithoutRestart()).isTrue();
    }

    @Test
    void invalidYamlIsRejected() {
        assertThatThrownBy(() -> loader.load(new StringReader("prefixes: [\n  - broken")))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Invalid YAML");
    }

    @Test
    void copiesDefaultFileWhenMissing(@TempDir Path dir) throws Exception {
        Path file = ConfigurationLoader.configFile(dir);
        loader.copyDefaultIfMissing(file);
        assertThat(Files.exists(file)).isTrue();
        PluginConfiguration config = loader.load(file);
        assertThat(config.prefix(AuthType.PREMIUM).value()).isEqualTo("&7[&aP&7] ");
    }

    @Test
    void unicodeAndEmptyPrefixesAreAccepted() throws Exception {
        PluginConfiguration config = loader.load(new StringReader("""
                prefixes:
                  premium:
                    enabled: true
                    value: "★ "
                  cracked:
                    enabled: true
                    value: ""
                """));
        assertThat(config.prefix(AuthType.PREMIUM).value()).isEqualTo("★ ");
        assertThat(config.prefix(AuthType.CRACKED).visible()).isFalse();
    }
}
