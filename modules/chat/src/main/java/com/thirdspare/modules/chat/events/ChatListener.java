package com.thirdspare.modules.chat.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.thirdspare.modules.chat.ChatService;
import com.thirdspare.modules.chat.data.ChatChannel;

import java.util.List;

public class ChatListener {
    private final ChatService chatService;

    public ChatListener(ChatService chatService) {
        this.chatService = chatService;
    }

    public void onPlayerChat(PlayerChatEvent event) {
        ChatChannel channel = chatService.resolveFocusChannel(event.getSender());
        List<com.hypixel.hytale.server.core.universe.PlayerRef> recipients =
                chatService.filterRecipients(event.getSender(), event.getTargets(), channel);
        event.setTargets(recipients);
        event.setFormatter((sender, content) -> chatService.format(channel, sender, content));
    }
}
