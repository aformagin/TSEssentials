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

public class ChannelCommand extends AbstractCommand {
    private final ChatService chatService;

    public ChannelCommand(ChatService chatService) {
        super("channel", "Manage chat channel subscriptions");
        this.chatService = chatService;
        addAliases("ch");
        addSubCommand(new ListSubCommand(chatService));
        addSubCommand(new FocusSubCommand(chatService));
        addSubCommand(new JoinSubCommand(chatService));
        addSubCommand(new LeaveSubCommand(chatService));
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        chatService.listChannels(player);
        return CompletableFuture.completedFuture(null);
    }

    private static final class ListSubCommand extends AbstractCommand {
        private final ChatService chatService;

        private ListSubCommand(ChatService chatService) {
            super("list", "List available chat channels");
            this.chatService = chatService;
        }

        @Override
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
            if (player != null) {
                chatService.listChannels(player);
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class FocusSubCommand extends AbstractCommand {
        private final ChatService chatService;
        private final RequiredArg<String> channelArg;

        private FocusSubCommand(ChatService chatService) {
            super("focus", "Set your active chat channel");
            this.chatService = chatService;
            this.channelArg = withRequiredArg("channel", "Channel name", ArgTypes.STRING);
        }

        @Override
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
            if (player != null) {
                chatService.focus(player, context.get(channelArg));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class JoinSubCommand extends AbstractCommand {
        private final ChatService chatService;
        private final RequiredArg<String> channelArg;

        private JoinSubCommand(ChatService chatService) {
            super("join", "Subscribe to a chat channel");
            this.chatService = chatService;
            this.channelArg = withRequiredArg("channel", "Channel name", ArgTypes.STRING);
        }

        @Override
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
            if (player != null) {
                chatService.join(player, context.get(channelArg));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class LeaveSubCommand extends AbstractCommand {
        private final ChatService chatService;
        private final RequiredArg<String> channelArg;

        private LeaveSubCommand(ChatService chatService) {
            super("leave", "Unsubscribe from a chat channel");
            this.chatService = chatService;
            this.channelArg = withRequiredArg("channel", "Channel name", ArgTypes.STRING);
        }

        @Override
        protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
            PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
            if (player != null) {
                chatService.leave(player, context.get(channelArg));
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
