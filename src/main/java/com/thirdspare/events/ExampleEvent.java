package com.thirdspare.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ExampleEvent {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Ref<EntityStore> entityRef = event.getPlayerRef();
        Store<EntityStore> store = entityRef.getStore();
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef != null) {
            playerRef.sendMessage(Message.raw("Welcome " + playerRef.getUsername()));
        }
    }
    //TODO On player join, check if they are a new player, if they are teleport them to spawn
    public static void onNewPlayerJoin(PlayerConnectEvent event) {
    }
}
