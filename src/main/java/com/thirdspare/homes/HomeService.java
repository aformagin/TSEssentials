package com.thirdspare.homes;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.data.PlayerDataConfig;
import com.thirdspare.data.PlayerHomeData;

public class HomeService {
    private final PlayerDataConfig legacyPlayerData;
    private final ComponentType<EntityStore, PlayerHomesComponent> homesComponentType;

    public HomeService(PlayerDataConfig legacyPlayerData,
                       ComponentType<EntityStore, PlayerHomesComponent> homesComponentType) {
        this.legacyPlayerData = legacyPlayerData;
        this.homesComponentType = homesComponentType;
    }

    /**
     * Must be called from the player's owning world thread.
     */
    public PlayerHomesComponent getHomes(PlayerRef player) {
        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref.getStore();
        PlayerHomesComponent homes = store.ensureAndGetComponent(ref, homesComponentType);
        migrateLegacyHomes(player, homes);
        return homes;
    }

    public PlayerHomeData getHome(PlayerRef player, String homeName) {
        return getHomes(player).getHome(homeName);
    }

    public void setHome(PlayerRef player, String homeName, PlayerHomeData homeData) {
        getHomes(player).setHome(homeName, homeData);
    }

    public boolean hasHome(PlayerRef player, String homeName) {
        return getHomes(player).hasHome(homeName);
    }

    public int getHomeCount(PlayerRef player) {
        return getHomes(player).getHomeCount();
    }

    public int getMaxHomes() {
        return legacyPlayerData.getMaxHomes();
    }

    private void migrateLegacyHomes(PlayerRef player, PlayerHomesComponent homes) {
        if (homes.isLegacyHomesMigrated()) {
            return;
        }
        homes.putLegacyHomes(legacyPlayerData.getHomes(player.getUuid()));
    }
}
