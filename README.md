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
-   **Economy System (V1):** Digital currency system with player accounts, admin management, and an in-game UI.
-   **Optional Permissions Module:** Drop-in plain-JAR module for TSEssentials-managed groups, permission nodes, memberships, and an in-game admin UI. It is additive and does not replace Hytale's default permission provider.
-   **Land Claims (Planned):** Robust protection system to prevent griefing.
-   **Concurrency & Safety:** All commands have been updated to be world-thread safe, ensuring stability and preventing race conditions even under heavy server load.
-   **Admin Teleport:** Administrators can force-teleport players to their location instantly.
-   **Simple Commands:** Intuitive and easy-to-use commands for all teleportation features.
-   **Configuration:** All data is stored in simple JSON files, making it easy to view, edit, or reset data if needed.

## TSEssentials Module System

TSEssentials features a robust, optional module system that allows for additive functionality without bloating the core plugin. Modules are plain JAR files that are discovered and loaded at runtime.

### Drop-in Modules

To install an optional module:
1.  Create a folder named `TSEssentialsModules` in the server's `UserData/Mods/` directory (beside the main `TSEssentials.jar`).
2.  Drop the module JAR (e.g., `TSEssentials-Permissions-1.1.0.jar`) into this folder.
3.  Restart the server.

### Discovery & Lifecycle

The core plugin automatically scans the `TSEssentialsModules` directory for any JAR files starting with `TSEssentials-`. Each module undergoes a strict lifecycle:
-   **Discovery:** JARs are identified and loaded into isolated classloaders.
-   **Registration:** Modules receive a `TSEModuleContext` to register their own configurations, commands, and ECS components.
-   **Enablement:** Modules are enabled after the core plugin has finished its own initialization.
-   **Player Sync:** Modules are notified when a player is ready, allowing for dynamic data synchronization.
-   **Shutdown:** Modules are cleanly disabled when the server stops, ensuring data integrity.

### Developer API

Developers can create their own modules by implementing the `TSEModule` interface and providing a `TSEModuleDescriptor`. Access to core services is provided through the `TSEModuleContext`, which includes:
-   **Config Registration:** Simple, codec-backed JSON configuration management.
-   **Command Registration:** Integration with Hytale's command system.
-   **ECS Component Registration:** Support for custom player or entity data.
-   **Event Access:** Access to the global `EventRegistry`.

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

### Economy Commands

-   `/balance [player]`
    -   **Description:** Checks your current balance or the balance of another player.
-   `/pay <player> <amount>`
    -   **Description:** Sends money to another player.
-   `/eco <give|take|set> <player> <amount>`
    -   **Description:** Admin command to manage player balances.
-   `/wallet`
    -   **Description:** Opens the player economy UI.
-   `/econadmin`
    -   **Description:** Opens the admin economy management UI.

### Admin Commands

-   `/tphere <player>`
    -   **Description:** Instantly teleports the specified player to your current location. No request or confirmation is needed.
    -   **Usage:** `/tphere Steve`

### Optional Permissions Module Commands

These commands are available only when `TSEssentials-Permissions-<version>.jar` is present beside the core TSEssentials plugin JAR.

-   `/tsperm`
    -   **Description:** Opens the permissions admin UI.
-   `/tsperm group list`
    -   **Description:** Lists local TSE permissions groups.
-   `/tsperm group create <group> [display-name]`
    -   **Description:** Creates a local permissions group.
-   `/tsperm group delete <group>`
    -   **Description:** Deletes a local permissions group and removes it from user records.
-   `/tsperm group addnode <group> <node>`
    -   **Description:** Adds a permission node to a group.
-   `/tsperm group removenode <group> <node>`
    -   **Description:** Removes a permission node from a group.
-   `/tsperm user groups <player-or-uuid>`
    -   **Description:** Shows local TSE group memberships.
-   `/tsperm user addgroup <player-or-uuid> <group>`
    -   **Description:** Adds a player to a local group.
-   `/tsperm user removegroup <player-or-uuid> <group>`
    -   **Description:** Removes a player from a local group.
-   `/tsperm test <player-or-uuid> <node>`
    -   **Description:** Tests the local TSE provider's effective permission result.
-   `/tsperm reload`
    -   **Description:** Reloads local permissions configs.

## Configuration Files

The plugin generates three configuration files in the server's data directory.

### `player_data.json`

This file stores information about each player's set homes.

-   `MaxHomes`: An integer that defines the maximum number of homes a single player is allowed to set.
-   `PlayerHomes`: An object where each key is a unique identifier for a player's home and the value is the location data for that home.
    -   The key is a string formatted as `"player-uuid:home-name"`. The default home uses `"player-uuid:default"`.
    -   The location data includes the world's UUID, coordinates (X, Y, Z), and rotation (Pitch, Yaw, Roll).

### `warp_data.json`

This file stores the locations of all server-wide warp points.

-   `Warps`: An object where each key is the name of the warp (in lowercase) and the value is the location data for that warp.

### `spawn_data.json`

This file stores the single, global server spawn point.

-   `Spawn`: An object containing the location data for the server spawn. If no spawn is set, this object will be absent or null.

## Optional Permissions Module

The permissions module builds separately from the core plugin:

```bash
mvn clean install
mvn -f modules/permissions/pom.xml clean package
```

Deploy `target/TSEssentials-<version>.jar` to the server mods directory. Deploy optional module JARs into a `TSEssentialsModules` folder beside the core plugin JAR, for example `UserData/Mods/TSEssentialsModules/TSEssentials-Permissions-<version>.jar`. The permissions module is a plain JAR with no Hytale `manifest.json`; core TSEssentials discovers it through Java `ServiceLoader`.

The module stores authoritative data in codec-backed JSON files under the core plugin data directory's `modules/permissions` folder:

-   `permissions_groups.json`: Local group definitions and permission nodes.
-   `permissions_users.json`: UUID-keyed group memberships and last-known usernames.

Player ECS data is only an online mirror of group memberships. Removing the permissions module JAR leaves the JSON files untouched, and core TSEssentials continues to boot without the module.

The permissions admin page is registered by the optional module, but its `PermissionsAdmin.ui` document is packaged with the core plugin resources because Hytale Custom UI document loader logic. The CLI commands remain the supported fallback if custom UI loading changes in a future runtime.
