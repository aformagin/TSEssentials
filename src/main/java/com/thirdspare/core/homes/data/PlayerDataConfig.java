package com.thirdspare.core.homes.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataConfig {
    private Map<String, PlayerHomeData> playerHomes;
    private int maxHomes;

    public PlayerDataConfig() {
        this.playerHomes = new HashMap<>();
        this.maxHomes = 1;
    }

    public PlayerDataConfig(Map<String, PlayerHomeData> playerHomes, int maxHomes) {
        this.playerHomes = playerHomes != null ? playerHomes : new HashMap<>();
        this.maxHomes = maxHomes > 0 ? maxHomes : 1;
    }

    private String getHomeKey(UUID playerUUID, String homeName) {
        if (homeName == null || homeName.isEmpty()) {
            return playerUUID.toString() + ":default";
        }
        return playerUUID.toString() + ":" + homeName.toLowerCase();
    }

    public PlayerHomeData getHome(UUID playerUUID, String homeName) {
        return playerHomes.get(getHomeKey(playerUUID, homeName));
    }

    public PlayerHomeData getHome(UUID playerUUID) {
        return getHome(playerUUID, null);
    }

    public void setHome(UUID playerUUID, String homeName, PlayerHomeData homeData) {
        playerHomes.put(getHomeKey(playerUUID, homeName), homeData);
    }

    public boolean hasHome(UUID playerUUID, String homeName) {
        return playerHomes.containsKey(getHomeKey(playerUUID, homeName));
    }

    public boolean hasHome(UUID playerUUID) {
        String uuidPrefix = playerUUID.toString() + ":";
        return playerHomes.keySet().stream().anyMatch(key -> key.startsWith(uuidPrefix));
    }

    public boolean removeHome(UUID playerUUID, String homeName) {
        return playerHomes.remove(getHomeKey(playerUUID, homeName)) != null;
    }

    public Map<String, PlayerHomeData> getPlayerHomes() {
        return playerHomes;
    }

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

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int maxHomes) {
        this.maxHomes = maxHomes > 0 ? maxHomes : 1;
    }

    public int getHomeCount(UUID playerUUID) {
        String uuidPrefix = playerUUID.toString() + ":";
        return (int) playerHomes.keySet().stream()
                .filter(key -> key.startsWith(uuidPrefix))
                .count();
    }

    public static final BuilderCodec<PlayerDataConfig> CODEC = BuilderCodec.builder(PlayerDataConfig.class, PlayerDataConfig::new)
            .append(new KeyedCodec<>("MaxHomes", Codec.INTEGER),
                    (config, val) -> config.maxHomes = val,
                    config -> config.maxHomes).add()
            .append(new KeyedCodec<>("PlayerHomes",
                    new MapCodec<>(
                            PlayerHomeData.CODEC,
                            HashMap::new
                    )),
                    (config, val) -> config.playerHomes = val != null ? new HashMap<>(val) : new HashMap<>(),
                    config -> config.playerHomes).add()
            .build();
}
