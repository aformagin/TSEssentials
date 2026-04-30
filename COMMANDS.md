# TSEssentials Commands

This document provides a comprehensive list of all commands available in TSEssentials, including those provided by optional modules.

## Core Commands

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

### Utility Commands

-   `/heal [player]`
    -   **Description:** Restores a player's health to full. If no player is specified, heals the sender.
-   `/repair`
    -   **Description:** Repairs the item currently held in the player's hand.
-   `/repairall`
    -   **Description:** Repairs all repairable items in the player's inventory, including hotbar, armor, and backpack.

### Teleport Request (TPA) Commands

-   `/tpa <player>`
    -   **Description:** Sends a request to teleport to another player. The target player must accept the request before the teleport occurs.
-   `/tpahere <player>`
    -   **Description:** Sends a request for another player to teleport to you.
-   `/tpaccept`
    -   **Description:** Accepts the most recent pending teleport request.
-   `/tpdeny`
    -   **Description:** Denies the most recent pending teleport request.
-   **Note:** Teleport requests expire after 120 seconds.

### Economy Commands

-   `/balance [player]`
    -   **Description:** Checks your current balance or the balance of another player.
-   `/pay <player> <amount>`
    -   **Description:** Sends money from your account to another player.
-   `/eco <give|take|set> <player> <amount>`
    -   **Description:** Administrative command to manage player balances.
-   `/wallet`
    -   **Description:** Opens the player economy UI.
-   `/econadmin`
    -   **Description:** Opens the admin economy management UI.

### Admin Utility Commands

-   `/tphere <player>`
    -   **Description:** Instantly teleports the specified player to your current location. No request or confirmation is needed.
    -   **Usage:** `/tphere Steve`

---

## Optional Module Commands

These commands are only available when the corresponding module JAR is present in the `TSEssentialsModules` folder.

### Chat Module Commands

-   `/ch <channel-name>`
    -   **Description:** Sets your focus to the specified channel.
-   `/ch join <channel-name>`
    -   **Description:** Joins a channel to receive its messages.
-   `/ch leave <channel-name>`
    -   **Description:** Leaves a channel.
-   `/nick <nickname>`
    -   **Description:** Sets your display name on the server.
-   `/ignore <player>`
    -   **Description:** Mutes messages from the specified player.

### Permissions Module Commands

-   `/tsperm`
    -   **Description:** Opens the permissions admin UI.
-   `/tsperm group list`
    -   **Description:** Lists local TSE permissions groups.
-   `/tsperm group create <group> [display-name]`
    -   **Description:** Creates a local permissions group.
-   `/tsperm group delete <group>`
    -   **Description:** Deletes a local permissions group.
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
    -   **Description:** Tests the effective permission result.
-   `/tsperm reload`
    -   **Description:** Reloads local permissions configurations.
