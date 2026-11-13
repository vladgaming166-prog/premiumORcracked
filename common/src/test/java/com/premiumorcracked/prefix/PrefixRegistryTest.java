package com.premiumorcracked.prefix;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PrefixView;
import com.premiumorcracked.config.DisplaySettings;
import com.premiumorcracked.config.FormattingSettings;
import com.premiumorcracked.config.PluginConfiguration;
import com.premiumorcracked.config.PluginSettings;
import com.premiumorcracked.config.PrefixDefinition;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixRegistryTest {
    private final PrefixFormatter formatter = new PrefixFormatter();

    @Test
    void disabledPrefixIsNotVisible() {
        PrefixRegistry registry = new PrefixRegistry(formatter, config(
                new PrefixDefinition(false, "&aP "),
                new PrefixDefinition(true, "&fC "),
                new PrefixDefinition(true, "? ")
        ));
        PrefixView premium = registry.view(AuthType.PREMIUM);
        assertThat(premium.enabled()).isFalse();
        assertThat(premium.visibleLegacyAmpersand()).isEmpty();
        assertThat(registry.view(AuthType.CRACKED).isVisible()).isTrue();
    }

    @Test
    void emptyValueHidesIndicatorEvenWhenEnabled() {
        PrefixRegistry registry = new PrefixRegistry(formatter, config(
                new PrefixDefinition(true, ""),
                new PrefixDefinition(true, "&fC "),
                new PrefixDefinition(true, "? ")
        ));
        assertThat(registry.view(AuthType.PREMIUM).isVisible()).isFalse();
    }

    @Test
    void updateReplacesCachedPrefixes() {
        PrefixRegistry registry = new PrefixRegistry(formatter, PluginConfiguration.defaults());
        assertThat(registry.view(AuthType.PREMIUM).rawValue()).isEqualTo("&7[&aP&7] ");
        registry.update(config(
                new PrefixDefinition(true, "&6PREM "),
                new PrefixDefinition(true, "&fCRACK "),
                new PrefixDefinition(true, "? ")
        ));
        assertThat(registry.view(AuthType.PREMIUM).rawValue()).isEqualTo("&6PREM ");
        assertThat(registry.view(AuthType.CRACKED).rawValue()).isEqualTo("&fCRACK ");
    }

    @Test
    void eachAuthTypeHasItsOwnPrefix() {
        PrefixRegistry registry = new PrefixRegistry(formatter, PluginConfiguration.defaults());
        assertThat(registry.view(AuthType.PREMIUM).rawValue()).isNotEqualTo(registry.view(AuthType.CRACKED).rawValue());
        assertThat(registry.view(AuthType.UNKNOWN).rawValue()).isNotEqualTo(registry.view(AuthType.PREMIUM).rawValue());
    }

    private static PluginConfiguration config(PrefixDefinition premium, PrefixDefinition cracked, PrefixDefinition unknown) {
        EnumMap<AuthType, PrefixDefinition> prefixes = new EnumMap<>(AuthType.class);
        prefixes.put(AuthType.PREMIUM, premium);
        prefixes.put(AuthType.CRACKED, cracked);
        prefixes.put(AuthType.UNKNOWN, unknown);
        return new PluginConfiguration(prefixes, DisplaySettings.defaults(), FormattingSettings.defaults(), PluginSettings.defaults());
    }
}
