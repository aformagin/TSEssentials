package com.thirdspare.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

public class ExampleEvent {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Message.raw("Welcome " + player.getDisplayName()));
    }
    //TODO On player join, check if they are a new player, if they are teleport them to spawn
    public static void onNewPlayerJoin(PlayerConnectEvent event) {
        Player player = event.getPlayer();
    }
}