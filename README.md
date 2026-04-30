# TSEssentials Hytale Plugin

## Overview

TSEssentials is a foundational Hytale server plugin designed to provide essential teleportation features for players and server administrators. It includes personal homes, server-wide warps, a global spawn point, and a player-to-player teleport request (TPA) system, making navigation across the game world seamless and efficient.

## Features

-   **Personal Homes:** Allows players to set one or more personal "home" locations that they can teleport back to at any time. The number of homes a player can set is configurable.
-   **Server Warps:** Enables server administrators to create a network of server-wide warp points, facilitating easy travel to key locations like cities, dungeons, or event areas.
-   **Server Spawn:** A global spawn point that any player can teleport to.
-   **Teleport Requests (TPA):** Players can request to teleport to each other, or request others to teleport to them. Requests expire after 120 seconds and support accept/deny workflows.
-   **Economy System (V1):** Digital currency system with player accounts, admin management, and an in-game UI.
-   **Modular Extensions:** Optional drop-in modules for enhanced functionality without core plugin bloat.
    -   **Chat Channels:** Modularized organization for communication with joinable channels, local range support, and nicknames.
    -   **Permissions:** Drop-in management for groups, nodes, and memberships with an in-game admin UI.
-   **Land Claims (Planned):** Robust protection system to prevent griefing.
-   **Concurrency & Safety:** All commands have been updated to be world-thread safe, ensuring stability and preventing race conditions even under heavy server load.
-   **Admin Teleport:** Administrators can force-teleport players to their location instantly.
-   **Simple Commands:** Intuitive and easy-to-use commands for all teleportation features.
-   **Configuration:** All data is stored in simple JSON files, making it easy to view, edit, or reset data if needed.

## TSEssentials Module System

TSEssentials features a robust, optional module system that allows for additive functionality. Modules are plain JAR files discovered and loaded at runtime.

### Drop-in Modules

To install an optional module:
1.  Create a folder named `TSEssentialsModules` in the server's `/Mods/` directory or client's `/UserData/Mods/` directory (beside the main `TSEssentials.jar`).
2.  Drop the module JAR (e.g., `TSEssentials-Chat-1.0.jar`) into this folder.
3.  Restart the server.

### Available Optional Modules

| Module | Description | Build Command |
| :--- | :--- | :--- |
| **Chat** | Multi-channel chat, range-based local chat, nicknames, and mutes. | `mvn -f modules/chat/pom.xml clean package` |
| **Permissions** | Group-based permissions, user records, and an in-game admin UI. | `mvn -f modules/permissions/pom.xml clean package` |

### Discovery & Lifecycle

The core plugin automatically scans the `TSEssentialsModules` directory for any JAR files starting with `TSEssentials-`. Each module undergoes a strict lifecycle:
-   **Discovery:** JARs are identified and loaded into isolated classloaders.
-   **Registration:** Modules receive a `TSEModuleContext` to register their own configurations, commands, and ECS components.
-   **Enablement:** Modules are enabled after the core plugin has finished its own initialization.
-   **Player Sync:** Modules are notified when a player is ready, allowing for dynamic data synchronization.
-   **Shutdown:** Modules are cleanly disabled when the server stops, ensuring data integrity.

## Commands

### Home Commands

-   `/sethome [home-name]`
    -   **Description:** Sets a player's home at their current location.
-   `/home [home-name]`
    -   **Description:** Teleports a player to their set home.

### Warp Commands

-   `/setwarp <warp-name>`
    -   **Description:** Creates or updates a server-wide warp point.
-   `/warp <warp-name>`
    -   **Description:** Teleports a player to the specified server warp.

### Spawn Commands

-   `/setspawn`
    -   **Description:** Sets the server's global spawn point.
-   `/spawn`
    -   **Description:** Teleports a player to the server's global spawn point.

### Teleport Request (TPA) Commands

-   `/tpa <player>`, `/tpahere <player>`, `/tpaccept`, `/tpdeny`
    -   **Note:** Teleport requests expire after 120 seconds.

### Economy Commands

-   `/balance [player]`, `/pay <player> <amount>`, `/eco <give|take|set> <player> <amount>`
-   `/wallet`, `/econadmin` (UI-based management)

### Optional Chat Module Commands

*Available only when `TSEssentials-Chat-<version>.jar` is present.*

-   `/ch <channel-name>`: Sets focus channel.
-   `/ch join <channel-name>`: Joins a channel.
-   `/ch leave <channel-name>`: Leaves a channel.
-   `/nick <nickname>`: Sets your display name.
-   `/ignore <player>`: Mutes a player.

### Optional Permissions Module Commands

*Available only when `TSEssentials-Permissions-<version>.jar` is present.*

-   `/tsperm`: Opens the permissions admin UI.
-   `/tsperm group <list|create|delete|addnode|removenode>`: Manage groups.
-   `/tsperm user <groups|addgroup|removegroup>`: Manage user memberships.
-   `/tsperm test <player> <node>`: Test effective permissions.

## Configuration Files

The plugin and its modules generate configuration files in the server's data directory.

-   **Core:** `player_data.json`, `warp_data.json`, `spawn_data.json`
-   **Chat Module:** `modules/chat/chat_channels.json`
-   **Permissions Module:** `modules/permissions/permissions_groups.json`, `modules/permissions/permissions_users.json`

## Deployment

1.  Build the core plugin: `mvn clean install`
2.  Build desired modules (see [Available Optional Modules](#available-optional-modules)).
3.  Deploy core JAR to `Mods/`.
4.  Deploy module JARs to `Mods/TSEssentialsModules/`.
