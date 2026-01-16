package com.thirdspare.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for accessing player respawn point data.
 * Provides methods to check if a player has a respawn point (e.g., bed) set
 * and to retrieve respawn point information.
 */
public class RespawnUtil {

    /**
     * Check if a player has any respawn points set in the specified world.
     *
     * @param player    The player to check
     * @param worldName The world name to check respawn points for
     * @return true if the player has at least one respawn point set
     */
    public static boolean hasRespawnPoint(@Nonnull Player player, @Nonnull String worldName) {
        PlayerRespawnPointData[] respawnPoints = getRespawnPoints(player, worldName);
        return respawnPoints != null && respawnPoints.length > 0;
    }

    /**
     * Get all respawn points for a player in the specified world.
     *
     * @param player    The player to get respawn points for
     * @param worldName The world name to get respawn points from
     * @return Array of respawn points, or null if none set
     */
    @Nullable
    public static PlayerRespawnPointData[] getRespawnPoints(@Nonnull Player player, @Nonnull String worldName) {
        PlayerConfigData configData = player.getPlayerConfigData();
        if (configData == null) {
            return null;
        }

        PlayerWorldData worldData = configData.getPerWorldData(worldName);
        if (worldData == null) {
            return null;
        }

        return worldData.getRespawnPoints();
    }

    /**
     * Get the primary (first) respawn point for a player in the specified world.
     *
     * @param player    The player to get the respawn point for
     * @param worldName The world name to get the respawn point from
     * @return The primary respawn point data, or null if none set
     */
    @Nullable
    public static PlayerRespawnPointData getPrimaryRespawnPoint(@Nonnull Player player, @Nonnull String worldName) {
        PlayerRespawnPointData[] respawnPoints = getRespawnPoints(player, worldName);
        if (respawnPoints == null || respawnPoints.length == 0) {
            return null;
        }
        return respawnPoints[0];
    }

    /**
     * Get the respawn position (coordinates) for a player's primary respawn point.
     *
     * @param player    The player to get the respawn position for
     * @param worldName The world name to get the respawn position from
     * @return The respawn position as Vector3d, or null if no respawn point set
     */
    @Nullable
    public static Vector3d getRespawnPosition(@Nonnull Player player, @Nonnull String worldName) {
        PlayerRespawnPointData respawnPoint = getPrimaryRespawnPoint(player, worldName);
        if (respawnPoint == null) {
            return null;
        }
        return respawnPoint.getRespawnPosition();
    }

    /**
     * Get the block position (e.g., bed location) for a player's primary respawn point.
     *
     * @param player    The player to get the block position for
     * @param worldName The world name to get the block position from
     * @return The block position as Vector3i, or null if no respawn point set
     */
    @Nullable
    public static Vector3i getRespawnBlockPosition(@Nonnull Player player, @Nonnull String worldName) {
        PlayerRespawnPointData respawnPoint = getPrimaryRespawnPoint(player, worldName);
        if (respawnPoint == null) {
            return null;
        }
        return respawnPoint.getBlockPosition();
    }

    /**
     * Get the name/identifier of a player's primary respawn point.
     *
     * @param player    The player to get the respawn point name for
     * @param worldName The world name to get the respawn point name from
     * @return The respawn point name, or null if no respawn point set
     */
    @Nullable
    public static String getRespawnPointName(@Nonnull Player player, @Nonnull String worldName) {
        PlayerRespawnPointData respawnPoint = getPrimaryRespawnPoint(player, worldName);
        if (respawnPoint == null) {
            return null;
        }
        return respawnPoint.getName();
    }

    /**
     * Get the count of respawn points a player has in the specified world.
     *
     * @param player    The player to count respawn points for
     * @param worldName The world name to count respawn points in
     * @return The number of respawn points (0 if none)
     */
    public static int getRespawnPointCount(@Nonnull Player player, @Nonnull String worldName) {
        PlayerRespawnPointData[] respawnPoints = getRespawnPoints(player, worldName);
        if (respawnPoints == null) {
            return 0;
        }
        return respawnPoints.length;
    }
}