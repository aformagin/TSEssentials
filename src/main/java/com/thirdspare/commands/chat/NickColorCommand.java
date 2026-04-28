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

public class NickColorCommand extends AbstractCommand {
    private final ChatService chatService;
    private final RequiredArg<String> colorArg;

    public NickColorCommand(ChatService chatService) {
        super("nickcolor", "Set or clear your nickname color");
        this.chatService = chatService;
        this.colorArg = withRequiredArg("color", "#RRGGBB or off", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player != null) {
            chatService.setNicknameColor(player, context.get(colorArg));
        }
        return CompletableFuture.completedFuture(null);
    }
}
