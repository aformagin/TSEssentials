package com.thirdspare.core.spawn.data;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class SpawnConfig {
    private SpawnData spawn;

    public SpawnConfig() {
        this.spawn = null;
    }

    public SpawnConfig(SpawnData spawn) {
        this.spawn = spawn;
    }

    public SpawnData getSpawn() {
        return spawn;
    }

    public void setSpawn(SpawnData spawnData) {
        this.spawn = spawnData;
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public void clearSpawn() {
        this.spawn = null;
    }

    public static final BuilderCodec<SpawnConfig> CODEC = BuilderCodec.builder(SpawnConfig.class, SpawnConfig::new)
            .append(new KeyedCodec<>("Spawn", SpawnData.CODEC),
                    (config, val) -> config.spawn = val,
                    config -> config.spawn).add()
            .build();
}
