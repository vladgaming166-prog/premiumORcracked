package com.premiumorcracked.placeholder;

import com.premiumorcracked.api.AuthType;
import com.premiumorcracked.api.PlayerPrefixInfo;
import com.premiumorcracked.api.PrefixView;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixPlaceholdersTest {
    @Test
    void resolvesRequiredPlaceholderIdentifiers() {
        PlayerPrefixInfo info = new PlayerPrefixInfo(
                UUID.randomUUID(),
                "Steve",
                AuthType.PREMIUM,
                new PrefixView(AuthType.PREMIUM, true, "&7[&aP&7] ", Component.text("[P] "), "&7[&aP&7] ", "§7[§aP§7] ")
        );
        assertThat(PrefixPlaceholders.resolve("prefix", info)).isEqualTo("&7[&aP&7] ");
        assertThat(PrefixPlaceholders.resolve("type", info)).isEqualTo("PREMIUM");
        assertThat(PrefixPlaceholders.resolve("display", info)).isEqualTo("&7[&aP&7] Steve");
        assertThat(PrefixPlaceholders.resolve("unknown-param", info)).isNull();
    }

    @Test
    void identifierIsStableForPlaceholderAPI() {
        assertThat(PrefixPlaceholders.IDENTIFIER).isEqualTo("premiumorcracked");
    }
}
