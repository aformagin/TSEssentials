package com.thirdspare.commands.chat;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.chat.ChatService;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ChannelMessageCommand extends AbstractCommand {
    private final ChatService chatService;
    private final String channelName;
    private final RequiredArg<String> messageArg;

    public ChannelMessageCommand(String command, String description, String channelName, ChatService chatService) {
        super(command, description);
        this.chatService = chatService;
        this.channelName = channelName;
        this.messageArg = withRequiredArg("message", "Message to send", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player != null) {
            chatService.sendDirect(player, channelName, context.get(messageArg));
        }
        return CompletableFuture.completedFuture(null);
    }
}
