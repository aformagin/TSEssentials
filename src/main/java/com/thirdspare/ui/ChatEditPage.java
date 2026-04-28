package com.thirdspare.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.thirdspare.chat.ChannelManager;
import com.thirdspare.data.chat.ChatChannel;

public class ChatEditPage extends BasicCustomUIPage {
    private final ChannelManager channelManager;

    public ChatEditPage(PlayerRef playerRef, ChannelManager channelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.channelManager = channelManager;
    }

    @Override
    public void build(UICommandBuilder builder) {
        StringBuilder content = new StringBuilder();
        for (ChatChannel channel : channelManager.getChannels()) {
            content.append(channel.getName())
                    .append(" ")
                    .append(channel.getPrefix())
                    .append(" color=")
                    .append(channel.getColor());
            if (channel.isRanged()) {
                content.append(" range=").append(channel.getRange());
            }
            if (channel.hasPermissionNode()) {
                content.append(" permission=").append(channel.getPermission());
            }
            content.append("\n");
        }
        builder.append("ChatEdit.ui");
        builder.set("#ChannelList.TextSpans", Message.raw(content.toString()));
        builder.set("#CommandHelp.TextSpans", Message.raw("/chatedit create|delete|prefix|color|range|permission|default"));
    }
}
