package com.thirdspare.utils;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.Optional;

/**
 * Utility class for looking up players by username
 */
public class PlayerLookup {

    /**
     * Find a player by their display name (case-insensitive)
     *
     * @param username The username to search for
     * @return Optional containing the PlayerRef if found
     */
    public static Optional<PlayerRef> findPlayerByName(String username) {
        if (username == null || username.isEmpty()) {
            return Optional.empty();
        }

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            if (playerRef.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(playerRef);
            }
        }

        return Optional.empty();
    }
}
