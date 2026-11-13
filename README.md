# PremiumOrCracked

Velocity + Paper plugin that labels players as Premium, Cracked, or Unknown with **fully customizable prefixes**. Velocity is the authentication authority. Every connected Paper server receives the same type and prefix so indicators never drift.

Authentication prefixes are independent of LuckPerms (and other rank plugins). Rank prefixes, suffixes and chat formats are never overwritten.

## Install

1. Put `PremiumOrCracked-Velocity-<version>.jar` in the Velocity `plugins/` folder.
2. Put `PremiumOrCracked-Paper-<version>.jar` in every Paper backend `plugins/` folder.
3. Optional: install PlaceholderAPI on Paper for `%premiumorcracked_*%` placeholders.
4. Start the proxy once to generate `plugins/premiumorcracked/config.yml`, then customize prefixes and run `/poc reload`.

## Prefixes (`config.yml`)

Edit values directly. No Java changes and no rebuild.

```yaml
prefixes:
  premium:
    enabled: true
    value: "&7[&aP&7] "
  cracked:
    enabled: true
    value: "&7[&fC&7] "
  unknown:
    enabled: true
    value: "&7[&8?&7] "

display:
  tablist: true
  chat: false
  nametag: false
  scoreboard: false

formatting:
  color-support: true
  legacy-colors: true
  minimessage: true

settings:
  update-prefix-without-restart: true
  cache-prefixes: true
```

Supported in `value`:

- Legacy codes (`&a`, `&l`, …)
- Hex (`#00FF00`, `&#00FF00`)
- MiniMessage (`<gray>[<green>PREMIUM</green>]</gray> `)
- Unicode / custom symbols
- Empty string to hide that indicator
- Independent `enabled` flags per type

## `/poc reload`

1. Reloads `config.yml`
2. Validates every prefix
3. Updates the prefix cache
4. Synchronizes the new config to connected Paper servers
5. Applies prefixes without restarting Velocity or Paper
6. Sends a success or error message (invalid configs are rejected and the previous prefixes stay in effect)

## Placeholders

| Placeholder | Result |
| --- | --- |
| `%premiumorcracked_prefix%` | Authentication prefix |
| `%premiumorcracked_type%` | `PREMIUM`, `CRACKED`, or `UNKNOWN` |
| `%premiumorcracked_display%` | Prefix + username |

Use these in TAB, LuckPerms display layouts, scoreboards, and chat plugins. Do not copy the authentication prefix into a LuckPerms rank prefix.

`display.tablist` prepends the authentication prefix to the current list name (including any rank prefix already applied). `display.chat` wraps the existing chat renderer instead of replacing it. Leave `nametag` and `scoreboard` off if another plugin already uses scoreboard teams for ranks.

## API

```java
PremiumOrCrackedApi api = PremiumOrCrackedProvider.get();
AuthType type = api.getAuthTypeOrUnknown(playerId);
String prefix = api.getPrefixLegacy(playerId);
String withRank = api.prependToExisting(playerId, luckPermsPrefix + name);
```

Paper also registers `PremiumOrCrackedApi` with the Bukkit services manager.

## Permissions

- `premiumorcracked.command` — `/poc`
- `premiumorcracked.reload` — `/poc reload`
