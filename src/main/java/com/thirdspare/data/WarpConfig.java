package com.thirdspare.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration class for storing all server warps
 * Warps are server-wide teleport points accessible to all players
 * Uses codec-based serialization for automatic JSON persistence
 */
public class WarpConfig {
    private Map<String, WarpData> warps;

    /**
     * Default no-arg constructor required for codec deserialization
     */
    public WarpConfig() {
        this.warps = new HashMap<>();
    }

    public WarpConfig(Map<String, WarpData> warps) {
        this.warps = warps != null ? warps : new HashMap<>();
    }

    /**
     * Get a warp by name
     * @param warpName The name of the warp (case-insensitive)
     * @return The warp data, or null if not found
     */
    public WarpData getWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return null;
        }
        return warps.get(warpName.toLowerCase());
    }

    /**
     * Set a warp location
     * @param warpName The name of the warp (will be converted to lowercase)
     * @param warpData The warp location data
     */
    public void setWarp(String warpName, WarpData warpData) {
        if (warpName == null || warpName.isEmpty()) {
            return;
        }
        warps.put(warpName.toLowerCase(), warpData);
    }

    /**
     * Check if a warp exists
     * @param warpName The name of the warp (case-insensitive)
     * @return true if the warp exists
     */
    public boolean hasWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return false;
        }
        return warps.containsKey(warpName.toLowerCase());
    }

    /**
     * Remove a warp
     * @param warpName The name of the warp (case-insensitive)
     * @return true if a warp was removed
     */
    public boolean removeWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return false;
        }
        return warps.remove(warpName.toLowerCase()) != null;
    }

    /**
     * Get all warp names
     * @return Set of all warp names
     */
    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    /**
     * Get the total number of warps
     * @return The number of warps
     */
    public int getWarpCount() {
        return warps.size();
    }

    /**
     * Get all warps (for internal use)
     * @return Map of warp names to warp data
     */
    public Map<String, WarpData> getWarps() {
        return warps;
    }

    /**
     * Codec for serializing/deserializing WarpConfig
     * This will automatically create/update warp_data.json
     */
    public static final BuilderCodec<WarpConfig> CODEC = BuilderCodec.builder(WarpConfig.class, WarpConfig::new)
            .append(new KeyedCodec<>("Warps",
                    new ObjectMapCodec<>(
                            WarpData.CODEC,
                            HashMap::new,
                            key -> key,  // String -> String (no conversion needed)
                            str -> str   // String -> String (no conversion needed)
                    )),
                    (config, val) -> config.warps = val,
                    config -> config.warps).add()
            .build();
}
