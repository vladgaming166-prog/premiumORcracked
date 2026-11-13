package com.premiumorcracked.display;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisplayComposerTest {
    @Test
    void neverReplacesLuckPermsPrefix() {
        String combined = DisplayComposer.prepend("&7[&aP&7] ", "&c[Admin] &fSteve");
        assertThat(combined).isEqualTo("&7[&aP&7] &c[Admin] &fSteve");
    }

    @Test
    void isIdempotent() {
        String once = DisplayComposer.prepend("&7[&aP&7] ", "Steve");
        String twice = DisplayComposer.prepend("&7[&aP&7] ", once);
        assertThat(twice).isEqualTo(once);
    }

    @Test
    void emptyAuthPrefixLeavesRankUntouched() {
        assertThat(DisplayComposer.prepend("", "&c[Mod] Steve")).isEqualTo("&c[Mod] Steve");
        assertThat(DisplayComposer.prepend(Component.empty(), Component.text("[Mod] Steve")))
                .isEqualTo(Component.text("[Mod] Steve"));
    }

    @Test
    void componentPrependKeepsExistingDisplay() {
        Component rank = Component.text("[Owner] ");
        Component name = Component.text("Steve");
        Component existing = Component.empty().append(rank).append(name);
        Component auth = Component.text("[P] ");
        Component combined = DisplayComposer.prepend(auth, existing);
        assertThat(combined.children()).isNotEmpty();
        assertThat(DisplayComposer.startsWith(combined, auth)).isTrue();
    }
}
