package com.thirdspare.commands.chat;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.chat.ChatService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class NickCommand extends AbstractCommand {
    private final ChatService chatService;
    private final RequiredArg<String> nicknameArg;

    public NickCommand(ChatService chatService) {
        super("nick", "Set or clear your chat nickname");
        requirePermission(TSEssentialsPermissions.NICK);
        this.chatService = chatService;
        this.nicknameArg = withRequiredArg("nickname", "Nickname or off", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player != null) {
            chatService.setNickname(player, context.get(nicknameArg));
        }
        return CompletableFuture.completedFuture(null);
    }
}
