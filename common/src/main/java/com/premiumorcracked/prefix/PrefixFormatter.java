package com.premiumorcracked.prefix;

import com.premiumorcracked.config.FormattingSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders prefix strings that may mix legacy codes, hex colors, MiniMessage and Unicode.
 */
public final class PrefixFormatter {
    private static final Pattern AMPERSAND_HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern BUKKIT_HEX = Pattern.compile("&[xX](&[0-9a-fA-F]){6}");
    private static final Pattern RAW_HEX = Pattern.compile("(?<![<&])#([0-9a-fA-F]{6})");
    private static final Pattern LEGACY_CODE = Pattern.compile("(?i)&([0-9a-fk-or])");

    private static final MiniMessage LENIENT = MiniMessage.miniMessage();
    private static final MiniMessage STRICT = MiniMessage.builder().strict(true).build();
    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.legacyAmpersand()
            .toBuilder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection()
            .toBuilder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static final int MAX_PREFIX_LENGTH = 256;

    public @NotNull Component render(@Nullable String raw, @NotNull FormattingSettings formatting) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        if (!formatting.colorSupport()) {
            return Component.text(stripFormatting(raw));
        }
        String prepared = raw;
        if (formatting.legacyColors()) {
            prepared = toMiniMessage(prepared);
        }
        if (formatting.miniMessage()) {
            try {
                return LENIENT.deserialize(prepared);
            } catch (RuntimeException ex) {
                if (formatting.legacyColors()) {
                    return AMPERSAND.deserialize(convertHexToLegacyAmpersand(raw));
                }
                return Component.text(raw);
            }
        }
        if (formatting.legacyColors()) {
            return AMPERSAND.deserialize(convertHexToLegacyAmpersand(raw));
        }
        return Component.text(raw);
    }

    public @NotNull String toLegacyAmpersand(@NotNull Component component) {
        return AMPERSAND.serialize(component);
    }

    public @NotNull String toLegacySection(@NotNull Component component) {
        return SECTION.serialize(component);
    }

    public void validate(@Nullable String raw, @NotNull FormattingSettings formatting, @NotNull String path, @NotNull java.util.List<String> errors) {
        if (raw == null) {
            errors.add(path + " value cannot be null");
            return;
        }
        if (raw.length() > MAX_PREFIX_LENGTH) {
            errors.add(path + " exceeds the maximum length of " + MAX_PREFIX_LENGTH + " characters");
        }
        validateHex(raw, path, errors);
        if (!formatting.colorSupport() || !formatting.miniMessage() || !looksLikeMiniMessage(raw)) {
            return;
        }
        try {
            STRICT.deserialize(raw);
        } catch (ParsingException ex) {
            errors.add(path + " has invalid MiniMessage: " + ex.getMessage());
        } catch (RuntimeException ex) {
            errors.add(path + " could not be parsed: " + ex.getMessage());
        }
    }

    public static @NotNull String stripFormatting(@NotNull String input) {
        String withoutHex = AMPERSAND_HEX.matcher(input).replaceAll("");
        withoutHex = BUKKIT_HEX.matcher(withoutHex).replaceAll("");
        withoutHex = RAW_HEX.matcher(withoutHex).replaceAll("");
        withoutHex = LEGACY_CODE.matcher(withoutHex).replaceAll("");
        withoutHex = withoutHex.replace("§", "");
        withoutHex = withoutHex.replaceAll("<#[0-9a-fA-F]{6}>", "");
        withoutHex = withoutHex.replaceAll("(?i)</?#?[a-z0-9_:-]+(?:[:][^>]*)?>", "");
        return withoutHex;
    }

    public static boolean looksLikeMiniMessage(@NotNull String input) {
        return input.indexOf('<') >= 0 && input.indexOf('>') > input.indexOf('<');
    }

    static @NotNull String toMiniMessage(@NotNull String input) {
        String value = convertBukkitHex(input);
        value = AMPERSAND_HEX.matcher(value).replaceAll("<#$1>");
        value = RAW_HEX.matcher(value).replaceAll("<#$1>");
        Matcher matcher = LEGACY_CODE.matcher(value);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement(legacyToMiniMessage(matcher.group(1))));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    static @NotNull String convertHexToLegacyAmpersand(@NotNull String input) {
        String value = convertBukkitHex(input);
        value = AMPERSAND_HEX.matcher(value).replaceAll("&#$1");
        value = RAW_HEX.matcher(value).replaceAll("&#$1");
        return value;
    }

    private static @NotNull String convertBukkitHex(@NotNull String input) {
        Matcher matcher = BUKKIT_HEX.matcher(input);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String digits = matcher.group().replaceAll("(?i)&x|&", "");
            matcher.appendReplacement(out, Matcher.quoteReplacement("&#" + digits));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static @NotNull String legacyToMiniMessage(@NotNull String code) {
        return switch (code.toLowerCase(Locale.ROOT)) {
            case "0" -> "<black>";
            case "1" -> "<dark_blue>";
            case "2" -> "<dark_green>";
            case "3" -> "<dark_aqua>";
            case "4" -> "<dark_red>";
            case "5" -> "<dark_purple>";
            case "6" -> "<gold>";
            case "7" -> "<gray>";
            case "8" -> "<dark_gray>";
            case "9" -> "<blue>";
            case "a" -> "<green>";
            case "b" -> "<aqua>";
            case "c" -> "<red>";
            case "d" -> "<light_purple>";
            case "e" -> "<yellow>";
            case "f" -> "<white>";
            case "k" -> "<obfuscated>";
            case "l" -> "<bold>";
            case "m" -> "<strikethrough>";
            case "n" -> "<underlined>";
            case "o" -> "<italic>";
            case "r" -> "<reset>";
            default -> "";
        };
    }

    private static void validateHex(@NotNull String raw, @NotNull String path, @NotNull java.util.List<String> errors) {
        Matcher ampersand = AMPERSAND_HEX.matcher(raw);
        while (ampersand.find()) {
            // valid by pattern
        }
        Matcher dangling = Pattern.compile("&#([0-9a-fA-F]{0,5})(?![0-9a-fA-F])").matcher(raw);
        while (dangling.find()) {
            if (dangling.group(1).length() != 6) {
                errors.add(path + " contains an incomplete hex color: " + dangling.group());
            }
        }
        Matcher bukkitPartial = Pattern.compile("&[xX]((?:&[0-9a-fA-F]){0,5})(?!&[0-9a-fA-F])").matcher(raw);
        while (bukkitPartial.find()) {
            if (bukkitPartial.group().replaceAll("(?i)&x|&", "").length() != 6) {
                errors.add(path + " contains an incomplete Bukkit hex color: " + bukkitPartial.group());
            }
        }
    }
}
