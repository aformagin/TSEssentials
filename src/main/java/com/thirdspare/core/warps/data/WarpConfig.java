package com.thirdspare.core.warps.data;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpConfig {
    private Map<String, WarpData> warps;

    public WarpConfig() {
        this.warps = new HashMap<>();
    }

    public WarpConfig(Map<String, WarpData> warps) {
        this.warps = warps != null ? warps : new HashMap<>();
    }

    public WarpData getWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return null;
        }
        return warps.get(warpName.toLowerCase());
    }

    public void setWarp(String warpName, WarpData warpData) {
        if (warpName == null || warpName.isEmpty()) {
            return;
        }
        warps.put(warpName.toLowerCase(), warpData);
    }

    public boolean hasWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return false;
        }
        return warps.containsKey(warpName.toLowerCase());
    }

    public boolean removeWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return false;
        }
        return warps.remove(warpName.toLowerCase()) != null;
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    public int getWarpCount() {
        return warps.size();
    }

    public Map<String, WarpData> getWarps() {
        return warps;
    }

    public static final BuilderCodec<WarpConfig> CODEC = BuilderCodec.builder(WarpConfig.class, WarpConfig::new)
            .append(new KeyedCodec<>("Warps",
                            new MapCodec<>(
                                    WarpData.CODEC,
                                    HashMap::new
                            )),
                    (config, val) -> config.warps = val != null ? new HashMap<>(val) : new HashMap<>(),
                    config -> config.warps).add()
            .build();
}
