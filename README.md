# TSEssentials Hytale Plugin

## Overview

TSEssentials is a foundational Hytale server plugin designed to provide essential teleportation features for players and server administrators. It includes personal homes, server-wide warps, a global spawn point, and a player-to-player teleport request (TPA) system, making navigation across the game world seamless and efficient.

## Features

-   **Personal Homes:** Allows players to set one or more personal "home" locations that they can teleport back to at any time. The number of homes a player can set is configurable.
-   **Server Warps:** Enables server administrators to create a network of server-wide warp points, facilitating easy travel to key locations like cities, dungeons, or event areas.
-   **Server Spawn:** A global spawn point that any player can teleport to.
-   **Teleport Requests (TPA):** Players can request to teleport to each other, or request others to teleport to them. Requests expire after 120 seconds and support accept/deny workflows.
-   **Chat Channels:** Organize communication with joinable channels (Global, Local, Staff). Supports range-based local chat and multi-channel subscriptions.
-   **Nicknames:** Allows players to set custom display names with color support.
-   **Economy & Shops (Planned):** Digital currency system and player-run marketplaces.
-   **Land Claims (Planned):** Robust protection system to prevent griefing.
-   **Concurrency & Safety:** All commands have been updated to be world-thread safe, ensuring stability and preventing race conditions even under heavy server load.
-   **Admin Teleport:** Administrators can force-teleport players to their location instantly.
-   **Simple Commands:** Intuitive and easy-to-use commands for all teleportation features.
-   **Configuration:** All data is stored in simple JSON files, making it easy to view, edit, or reset data if needed.

## Commands

### Home Commands

-   `/sethome [home-name]`
    -   **Description:** Sets a player's home at their current location. If no `home-name` is provided, it sets the default home.
    -   **Usage:** Stand at the desired location and type `/sethome` or `/sethome mybase`.
-   `/home [home-name]`
    -   **Description:** Teleports a player to their set home. If no `home-name` is provided, it teleports them to their default home.
    -   **Usage:** `/home` or `/home mybase`.
-   **Note:** The maximum number of homes a player can set is configurable in `player_data.json`.

### Warp Commands

-   `/setwarp <warp-name>`
    -   **Description:** Creates or updates a server-wide warp point at the player's current location. This command typically requires administrative privileges.
    -   **Usage:** `/setwarp spawn`.
-   `/warp <warp-name>`
    -   **Description:** Teleports a player to the specified server warp.
    -   **Usage:** `/warp spawn`.

### Spawn Commands

-   `/setspawn`
    -   **Description:** Sets the server's global spawn point at your current location. This command typically requires administrative privileges.
    -   **Usage:** Stand at the desired location and type `/setspawn`.
-   `/spawn`
    -   **Description:** Teleports a player to the server's global spawn point.
    -   **Usage:** `/spawn`.

### Teleport Request (TPA) Commands

-   `/tpa <player>`
    -   **Description:** Sends a request to teleport to another player. The target player must accept the request before the teleport occurs.
    -   **Usage:** `/tpa Steve`
-   `/tpahere <player>`
    -   **Description:** Sends a request for another player to teleport to you. The target player must accept the request before the teleport occurs.
    -   **Usage:** `/tpahere Steve`
-   `/tpaccept`
    -   **Description:** Accepts the most recent pending teleport request.
    -   **Usage:** `/tpaccept`
-   `/tpdeny`
    -   **Description:** Denies the most recent pending teleport request.
    -   **Usage:** `/tpdeny`
-   **Note:** Teleport requests expire after 120 seconds. Only one request per sender-target pair is active at a time; sending a new request replaces any existing one.

### Chat Commands

-   `/ch <channel-name>`
    -   **Description:** Sets your focus to the specified channel. Any messages you type will be sent there by default.
    -   **Usage:** `/ch global` or `/ch local`
-   `/ch join <channel-name>`
    -   **Description:** Joins a channel so you can receive its messages.
    -   **Usage:** `/ch join staff`
-   `/ch leave <channel-name>`
    -   **Description:** Leaves a channel.
    -   **Usage:** `/ch leave global`
-   `/nick <nickname>`
    -   **Description:** Sets your display name on the server.
    -   **Usage:** `/nick SuperSteve`
-   `/ignore <player>`
    -   **Description:** Mutes messages from the specified player.
    -   **Usage:** `/ignore GrieferDan`

### Admin Commands

-   `/tphere <player>`
    -   **Description:** Instantly teleports the specified player to your current location. No request or confirmation is needed.
    -   **Usage:** `/tphere Steve`

## Configuration Files

The plugin generates three configuration files in the server's data directory.

### `player_data.json`

This file stores information about each player's set homes.

-   `MaxHomes`: An integer that defines the maximum number of homes a single player is allowed to set.
-   `PlayerHomes`: An object where each key is a unique identifier for a player's home and the value is the location data for that home.
    -   The key is a string formatted as `"player-uuid:home-name"`. The default home uses `"player-uuid:default"`.
    -   The location data includes the world's UUID, coordinates (X, Y, Z), and rotation (Pitch, Yaw, Roll).

**Example `player_data.json`:**
```json
{
  "MaxHomes": 2,
  "PlayerHomes": {
    "a1b2c3d4-e5f6-7890-1234-567890abcdef:default": {
      "WorldUUID": "a-world-uuid-string",
      "X": 150.5,
      "Y": 64.0,
      "Z": -200.2,
      "Pitch": 0.0,
      "Yaw": -90.0,
      "Roll": 0.0
    },
    "a1b2c3d4-e5f6-7890-1234-567890abcdef:mining_outpost": {
      "WorldUUID": "a-world-uuid-string",
      "X": 3000.0,
      "Y": 45.0,
      "Z": 1500.7,
      "Pitch": 15.0,
      "Yaw": 45.5,
      "Roll": 0.0
    }
  }
}
```

### `warp_data.json`

This file stores the locations of all server-wide warp points.

-   `Warps`: An object where each key is the name of the warp (in lowercase) and the value is the location data for that warp.

**Example `warp_data.json`:**
```json
{
  "Warps": {
    "spawn": {
      "WorldUUID": "a-world-uuid-string",
      "X": 0.0,
      "Y": 70.0,
      "Z": 0.0,
      "Pitch": 0.0,
      "Yaw": 180.0,
      "Roll": 0.0
    },
    "market": {
      "WorldUUID": "a-world-uuid-string",
      "X": -120.5,
      "Y": 72.0,
      "Z": 55.8,
      "Pitch": 0.0,
      "Yaw": 0.0,
      "Roll": 0.0
    }
  }
}
```

### `spawn_data.json`

This file stores the single, global server spawn point.

-   `Spawn`: An object containing the location data for the server spawn. If no spawn is set, this object will be absent or null.
    -   The location data includes the world's UUID, coordinates (X, Y, Z), and rotation (Pitch, Yaw, Roll).

**Example `spawn_data.json` (Spawn Set):**
```json
{
  "Spawn": {
    "WorldUUID": "another-world-uuid-string",
    "X": 100.0,
    "Y": 60.0,
    "Z": 100.0,
    "Pitch": 0.0,
    "Yaw": 0.0,
    "Roll": 0.0
  }
}
```

**Example `spawn_data.json` (No Spawn Set):**
```json
{}
```

## Permissions

*(Note: This is a planned feature and not yet implemented)*

Future versions of this plugin will include a permissions system to control who can use certain commands. For example:
-   `tsessentials.setwarp`: Allows a user to create and update warps. (Default: OP only)
-   `tsessentials.home.multiple`: Allows a user to set multiple homes up to the configured limit.
