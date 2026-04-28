package com.thirdspare.commands.chat;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.chat.ChatService;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ChannelCommand extends AbstractCommand {
    private final ChatService chatService;
    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> channelArg;

    public ChannelCommand(ChatService chatService) {
        super("channel", "Manage chat channel subscriptions");
        this.chatService = chatService;
        this.actionArg = withRequiredArg("action", "list, focus, join, or leave", ArgTypes.STRING);
        this.channelArg = withOptionalArg("channel", "Channel name", ArgTypes.STRING);
        addAliases("ch");
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        String action = context.get(actionArg).toLowerCase();
        String channel = context.get(channelArg);

        switch (action) {
            case "list" -> chatService.listChannels(player);
            case "focus" -> {
                if (channel == null || channel.isBlank()) {
                    player.sendMessage(Message.raw("Usage: /channel focus <channel>").color("#FF6B6B"));
                } else {
                    chatService.focus(player, channel);
                }
            }
            case "join" -> {
                if (channel == null || channel.isBlank()) {
                    player.sendMessage(Message.raw("Usage: /channel join <channel>").color("#FF6B6B"));
                } else {
                    chatService.join(player, channel);
                }
            }
            case "leave" -> {
                if (channel == null || channel.isBlank()) {
                    player.sendMessage(Message.raw("Usage: /channel leave <channel>").color("#FF6B6B"));
                } else {
                    chatService.leave(player, channel);
                }
            }
            default -> player.sendMessage(Message.raw("Usage: /channel list|focus|join|leave [channel]").color("#FF6B6B"));
        }

        return CompletableFuture.completedFuture(null);
    }
}
