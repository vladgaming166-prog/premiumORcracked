package com.premiumorcracked.config;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.prefix.PrefixFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ConfigurationValidator {
    private final PrefixFormatter formatter;

    public ConfigurationValidator() {
        this(new PrefixFormatter());
    }

    public ConfigurationValidator(@NotNull PrefixFormatter formatter) {
        this.formatter = formatter;
    }

    public @NotNull ValidationResult validate(@Nullable PluginConfiguration configuration) {
        List<String> errors = new ArrayList<>();
        if (configuration == null) {
            return ValidationResult.failed(List.of("Configuration is missing"));
        }
        for (AuthType type : AuthType.values()) {
            PrefixDefinition definition = configuration.prefix(type);
            if (definition == null) {
                errors.add("prefixes." + type.key() + " is missing");
                continue;
            }
            formatter.validate(
                    definition.value(),
                    configuration.formatting(),
                    "prefixes." + type.key(),
                    errors
            );
        }
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.failed(errors);
    }
}
