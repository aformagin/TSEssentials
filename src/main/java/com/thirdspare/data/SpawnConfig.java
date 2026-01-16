package com.thirdspare.data;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Configuration class for storing the server spawn location
 * Holds a single global spawn point that all players can teleport to
 * Uses codec-based serialization for automatic JSON persistence
 */
public class SpawnConfig {
    private SpawnData spawn;

    /**
     * Default no-arg constructor required for codec deserialization
     */
    public SpawnConfig() {
        this.spawn = null;
    }

    public SpawnConfig(SpawnData spawn) {
        this.spawn = spawn;
    }

    /**
     * Get the spawn location
     * @return The spawn data, or null if not set
     */
    public SpawnData getSpawn() {
        return spawn;
    }

    /**
     * Set the spawn location
     * @param spawnData The spawn location data
     */
    public void setSpawn(SpawnData spawnData) {
        this.spawn = spawnData;
    }

    /**
     * Check if a spawn location has been set
     * @return true if spawn is set
     */
    public boolean hasSpawn() {
        return spawn != null;
    }

    /**
     * Clear the spawn location
     */
    public void clearSpawn() {
        this.spawn = null;
    }

    /**
     * Codec for serializing/deserializing SpawnConfig
     * This will automatically create/update spawn_data.json
     */
    public static final BuilderCodec<SpawnConfig> CODEC = BuilderCodec.builder(SpawnConfig.class, SpawnConfig::new)
            .append(new KeyedCodec<>("Spawn", SpawnData.CODEC),
                    (config, val) -> config.spawn = val,
                    config -> config.spawn).add()
            .build();
}