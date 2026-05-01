package com.thirdspare.core.homes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.core.homes.data.PlayerHomeData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerHomesComponent implements Component<EntityStore> {
    private Map<String, PlayerHomeData> homes;
    private boolean legacyHomesMigrated;

    public PlayerHomesComponent() {
        this.homes = new HashMap<>();
        this.legacyHomesMigrated = false;
    }

    public PlayerHomesComponent(PlayerHomesComponent other) {
        this.homes = new HashMap<>(other.homes);
        this.legacyHomesMigrated = other.legacyHomesMigrated;
    }

    @Override
    public Component<EntityStore> clone() {
        return new PlayerHomesComponent(this);
    }

    public PlayerHomeData getHome(String homeName) {
        return homes.get(normalizeHomeName(homeName));
    }

    public void setHome(String homeName, PlayerHomeData homeData) {
        homes.put(normalizeHomeName(homeName), homeData);
    }

    public boolean hasHome(String homeName) {
        return homes.containsKey(normalizeHomeName(homeName));
    }

    public int getHomeCount() {
        return homes.size();
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public void putLegacyHomes(Map<String, PlayerHomeData> legacyHomes) {
        if (legacyHomes != null) {
            homes.putAll(legacyHomes);
        }
        legacyHomesMigrated = true;
    }

    public boolean isLegacyHomesMigrated() {
        return legacyHomesMigrated;
    }

    public static String normalizeHomeName(String homeName) {
        return homeName == null || homeName.isBlank() ? "default" : homeName.trim().toLowerCase();
    }

    public static final BuilderCodec<PlayerHomesComponent> CODEC =
            BuilderCodec.builder(PlayerHomesComponent.class, PlayerHomesComponent::new)
                    .append(new KeyedCodec<>("Homes",
                            new ObjectMapCodec<>(
                                    PlayerHomeData.CODEC,
                                    HashMap::new,
                                    key -> key,
                                    str -> str
                            )),
                            (component, value) -> component.homes = value != null ? new HashMap<>(value) : new HashMap<>(),
                            component -> component.homes).add()
                    .append(new KeyedCodec<>("LegacyHomesMigrated", com.hypixel.hytale.codec.Codec.BOOLEAN),
                            (component, value) -> component.legacyHomesMigrated = value,
                            component -> component.legacyHomesMigrated).add()
                    .build();
}
