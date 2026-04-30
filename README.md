# TSEssentials Hytale Plugin

## Overview

TSEssentials is a foundational Hytale server plugin designed to provide essential teleportation features for players and server administrators. It includes personal homes, server-wide warps, a global spawn point, and a player-to-player teleport request (TPA) system, making navigation across the game world seamless and efficient.

## Features

-   **Personal Homes:** Allows players to set one or more personal "home" locations.
-   **Server Warps:** Enables server administrators to create a network of server-wide warp points.
-   **Server Spawn:** A global spawn point that any player can teleport to.
-   **Teleport Requests (TPA):** Players can request to teleport to each other.
-   **Economy System (V1):** Digital currency system with player accounts and admin management.
-   **Modular Extensions:** Optional drop-in modules for enhanced functionality.
    -   **Chat Channels (V1):** Joinable channels, range support, and nicknames.
    -   **Permissions (V1):** Group/node management with an in-game admin UI.
-   **Utility Tools:** Core commands for healing and item repair.
-   **Concurrency & Safety:** World-thread safe commands.
-   **Configuration:** Data stored in simple, human-readable JSON files.

## Documentation

-   [**Command Reference**](COMMANDS.md): Detailed information on all core and modular commands.
-   [**Permissions Progress Report**](Research/Planning%20Documents/Permissions%20Progress%20Report.md): Detailed implementation status of the modular systems.

## TSEssentials Module System

TSEssentials features a robust, optional module system that allows for additive functionality.

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
