package com.premiumorcracked.config;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.prefix.PrefixFormatter;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationValidatorTest {
    private final ConfigurationValidator validator = new ConfigurationValidator(new PrefixFormatter());

    @Test
    void defaultConfigurationIsValid() {
        ValidationResult result = validator.validate(PluginConfiguration.defaults());
        assertThat(result.errors()).isEmpty();
        assertThat(result.success()).isTrue();
    }

    @Test
    void unclosedMiniMessageIsRejected() {
        PluginConfiguration config = withPrefix(AuthType.PREMIUM, "<red>PREMIUM");
        ValidationResult result = validator.validate(config);
        assertThat(result.success()).isFalse();
        assertThat(result.formattedErrors()).contains("prefixes.premium");
    }

    @Test
    void incompleteHexIsRejected() {
        ValidationResult result = validator.validate(withPrefix(AuthType.CRACKED, "&#FFF "));
        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("hex"));
    }

    @Test
    void oversizedPrefixIsRejected() {
        String huge = "A".repeat(PrefixFormatter.MAX_PREFIX_LENGTH + 1);
        ValidationResult result = validator.validate(withPrefix(AuthType.UNKNOWN, huge));
        assertThat(result.success()).isFalse();
        assertThat(result.formattedErrors()).contains("maximum length");
    }

    @Test
    void validMiniMessageAndHexPass() {
        PluginConfiguration mini = withPrefix(AuthType.PREMIUM, "<gray>[<green>PREMIUM</green>]</gray> ");
        PluginConfiguration hex = withPrefix(AuthType.CRACKED, "&#00FF00[C] ");
        assertThat(validator.validate(mini).errors()).isEmpty();
        assertThat(validator.validate(hex).errors()).isEmpty();
    }

    private static PluginConfiguration withPrefix(AuthType type, String value) {
        PluginConfiguration defaults = PluginConfiguration.defaults();
        EnumMap<AuthType, PrefixDefinition> prefixes = new EnumMap<>(AuthType.class);
        prefixes.putAll(defaults.prefixes());
        prefixes.put(type, new PrefixDefinition(true, value));
        return new PluginConfiguration(prefixes, defaults.display(), defaults.formatting(), defaults.settings());
    }
}
