package com.premiumorcracked.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PocCommandParserTest {
    @Test
    void parsesReload() {
        assertThat(PocCommandParser.parse(new String[]{"reload"})).isEqualTo(PocCommandParser.Action.RELOAD);
        assertThat(PocCommandParser.parse(new String[]{"RELOAD"})).isEqualTo(PocCommandParser.Action.RELOAD);
    }

    @Test
    void helpWhenEmpty() {
        assertThat(PocCommandParser.parse(new String[]{})).isEqualTo(PocCommandParser.Action.HELP);
        assertThat(PocCommandParser.parse(null)).isEqualTo(PocCommandParser.Action.HELP);
    }

    @Test
    void suggestsSubcommands() {
        assertThat(PocCommandParser.suggestions(new String[]{"re"})).containsExactly("reload");
        assertThat(PocCommandParser.suggestions(new String[]{})).contains("reload", "status", "help");
    }
}
