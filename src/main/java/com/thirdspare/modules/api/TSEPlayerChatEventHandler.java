package com.thirdspare.modules.api;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;

/**
 * Optional module hook for player chat events.
 */
public interface TSEPlayerChatEventHandler {
    void onPlayerChat(PlayerChatEvent event);
}
