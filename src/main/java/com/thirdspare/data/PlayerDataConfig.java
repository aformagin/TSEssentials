package com.thirdspare.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration class for storing all player home data
 * Uses codec-based serialization for automatic JSON persistence
 */
public class PlayerDataConfig {
    private Map<String, PlayerHomeData> playerHomes;
    private int maxHomes;

    /**
     * Default no-arg constructor required for codec deserialization
     */
    public PlayerDataConfig() {
        this.playerHomes = new HashMap<>();
        this.maxHomes = 1; // Default to 1 home
    }

    public PlayerDataConfig(Map<String, PlayerHomeData> playerHomes, int maxHomes) {
        this.playerHomes = playerHomes != null ? playerHomes : new HashMap<>();
        this.maxHomes = maxHomes > 0 ? maxHomes : 1; // Ensure at least 1
    }

    /**
     * Generate a key for storing home data
     * @param playerUUID The player's UUID
     * @param homeName The name of the home (null for default)
     * @return The storage key
     */
    private String getHomeKey(UUID playerUUID, String homeName) {
        if (homeName == null || homeName.isEmpty()) {
            return playerUUID.toString() + ":default";
        }
        return playerUUID.toString() + ":" + homeName.toLowerCase();
    }

    /**
     * Get a player's home data by name
     * @param playerUUID The player's UUID
     * @param homeName The name of the home (null for default)
     * @return The home data, or null if not set
     */
    public PlayerHomeData getHome(UUID playerUUID, String homeName) {
        return playerHomes.get(getHomeKey(playerUUID, homeName));
    }

    /**
     * Get a player's default home (for backwards compatibility)
     * @param playerUUID The player's UUID
     * @return The home data, or null if not set
     */
    public PlayerHomeData getHome(UUID playerUUID) {
        return getHome(playerUUID, null);
    }

    /**
     * Set a player's home data
     * @param playerUUID The player's UUID
     * @param homeName The name of the home (null for default)
     * @param homeData The home location data
     */
    public void setHome(UUID playerUUID, String homeName, PlayerHomeData homeData) {
        playerHomes.put(getHomeKey(playerUUID, homeName), homeData);
    }

    /**
     * Check if a player has a specific home set
     * @param playerUUID The player's UUID
     * @param homeName The name of the home (null for default)
     * @return true if the player has this home
     */
    public boolean hasHome(UUID playerUUID, String homeName) {
        return playerHomes.containsKey(getHomeKey(playerUUID, homeName));
    }

    /**
     * Check if a player has any home set
     * @param playerUUID The player's UUID
     * @return true if the player has any home
     */
    public boolean hasHome(UUID playerUUID) {
        String uuidPrefix = playerUUID.toString() + ":";
        return playerHomes.keySet().stream().anyMatch(key -> key.startsWith(uuidPrefix));
    }

    /**
     * Remove a player's specific home
     * @param playerUUID The player's UUID
     * @param homeName The name of the home (null for default)
     * @return true if a home was removed
     */
    public boolean removeHome(UUID playerUUID, String homeName) {
        return playerHomes.remove(getHomeKey(playerUUID, homeName)) != null;
    }

    /**
     * Get all player homes (for internal use)
     * @return Map of player UUIDs to home data
     */
    public Map<String, PlayerHomeData> getPlayerHomes() {
        return playerHomes;
    }

    /**
     * Get legacy homes for a player keyed by ECS home name.
     * This supports one-time migration into PlayerHomesComponent.
     *
     * @param playerUUID The player's UUID
     * @return Map of normalized home names to home data
     */
    public Map<String, PlayerHomeData> getHomes(UUID playerUUID) {
        Map<String, PlayerHomeData> homes = new HashMap<>();
        String uuidPrefix = playerUUID.toString() + ":";
        playerHomes.forEach((key, value) -> {
            if (key.startsWith(uuidPrefix)) {
                String homeName = key.substring(uuidPrefix.length());
                homes.put(homeName == null || homeName.isBlank() ? "default" : homeName.toLowerCase(), value);
            }
        });
        return homes;
    }

    /**
     * Get the maximum number of homes allowed per player
     * @return The max homes limit
     */
    public int getMaxHomes() {
        return maxHomes;
    }

    /**
     * Set the maximum number of homes allowed per player
     * @param maxHomes The max homes limit (must be at least 1)
     */
    public void setMaxHomes(int maxHomes) {
        this.maxHomes = maxHomes > 0 ? maxHomes : 1;
    }

    /**
     * Get the number of homes a player currently has
     * @param playerUUID The player's UUID
     * @return The number of homes set
     */
    public int getHomeCount(UUID playerUUID) {
        String uuidPrefix = playerUUID.toString() + ":";
        return (int) playerHomes.keySet().stream()
                .filter(key -> key.startsWith(uuidPrefix))
                .count();
    }

    /**
     * Codec for serializing/deserializing PlayerDataConfig
     * This will automatically create/update player_data.json
     */
    public static final BuilderCodec<PlayerDataConfig> CODEC = BuilderCodec.builder(PlayerDataConfig.class, PlayerDataConfig::new)
            .append(new KeyedCodec<>("MaxHomes", Codec.INTEGER),
                    (config, val) -> config.maxHomes = val,
                    config -> config.maxHomes).add()
            .append(new KeyedCodec<>("PlayerHomes",
                    new ObjectMapCodec<>(
                            PlayerHomeData.CODEC,
                            HashMap::new,
                            key -> key,  // String -> String (no conversion needed)
                            str -> str   // String -> String (no conversion needed)
                    )),
                    (config, val) -> config.playerHomes = val != null ? new HashMap<>(val) : new HashMap<>(),
                    config -> config.playerHomes).add()
            .build();
}
