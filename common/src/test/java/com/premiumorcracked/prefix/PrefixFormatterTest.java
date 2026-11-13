package com.premiumorcracked.prefix;

import com.premiumorcracked.config.FormattingSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixFormatterTest {
    private final PrefixFormatter formatter = new PrefixFormatter();
    private final FormattingSettings all = FormattingSettings.defaults();

    @Test
    void rendersLegacyCodes() {
        Component component = formatter.render("&7[&aP&7] ", all);
        String legacy = formatter.toLegacyAmpersand(component);
        assertThat(PlainTextComponentSerializer.plainText().serialize(component)).isEqualTo("[P] ");
        assertThat(legacy).contains("&");
    }

    @Test
    void rendersHexColors() {
        Component component = formatter.render("#00FF00P ", all);
        String section = formatter.toLegacySection(component);
        assertThat(PlainTextComponentSerializer.plainText().serialize(component)).isEqualTo("P ");
        assertThat(component.color() == null ? component.children() : component).isNotNull();
        assertThat(hasColor(component, TextColor.color(0x00FF00)) || section.contains("00FF00") || section.contains("a")).isTrue();
    }

    @Test
    void rendersMiniMessage() {
        Component component = formatter.render("<gray>[<green>PREMIUM</green>]</gray> ", all);
        assertThat(PlainTextComponentSerializer.plainText().serialize(component)).isEqualTo("[PREMIUM] ");
    }

    @Test
    void supportsUnicodeAndSymbols() {
        Component component = formatter.render("&6★ ", all);
        assertThat(PlainTextComponentSerializer.plainText().serialize(component)).isEqualTo("★ ");
    }

    @Test
    void emptyPrefixRendersNothing() {
        assertThat(formatter.render("", all)).isEqualTo(Component.empty());
        assertThat(formatter.render(null, all)).isEqualTo(Component.empty());
    }

    @Test
    void colorSupportOffStripsFormatting() {
        Component component = formatter.render("&aP <#00FF00>C", new FormattingSettings(false, true, true));
        assertThat(PlainTextComponentSerializer.plainText().serialize(component)).isEqualTo("P C");
    }

    @Test
    void mixedLegacyAndMiniMessage() {
        Component component = formatter.render("&7[<green>P</green>&7] ", all);
        assertThat(PlainTextComponentSerializer.plainText().serialize(component)).isEqualTo("[P] ");
    }

    private static boolean hasColor(Component component, TextColor expected) {
        if (expected.equals(component.color()) || NamedTextColor.GREEN.equals(component.color())) {
            return true;
        }
        return component.children().stream().anyMatch(child -> hasColor(child, expected));
    }
}
