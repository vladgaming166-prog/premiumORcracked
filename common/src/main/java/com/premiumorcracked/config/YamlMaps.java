package com.premiumorcracked.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class YamlMaps {
    private YamlMaps() {
    }

    @SuppressWarnings("unchecked")
    static @NotNull Map<String, Object> asMap(@Nullable Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return new LinkedHashMap<>();
    }

    static boolean bool(@Nullable Map<String, Object> map, @NotNull String key, boolean fallback) {
        if (map == null) {
            return fallback;
        }
        Object value = first(map, key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on")) {
                return true;
            }
            if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("off")) {
                return false;
            }
        }
        return fallback;
    }

    static @NotNull String string(@Nullable Map<String, Object> map, @NotNull String key, @NotNull String fallback) {
        if (map == null) {
            return fallback;
        }
        Object value = first(map, key);
        return value == null ? fallback : String.valueOf(value);
    }

    static @Nullable Object first(@NotNull Map<String, Object> map, @NotNull String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        String lower = key.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(lower)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
