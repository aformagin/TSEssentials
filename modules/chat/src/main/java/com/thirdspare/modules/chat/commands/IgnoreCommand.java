package com.thirdspare.modules.chat.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.chat.ChatService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class IgnoreCommand extends AbstractCommand {
    private final ChatService chatService;
    private final boolean ignore;
    private final RequiredArg<String> usernameArg;

    public IgnoreCommand(String name, String description, boolean ignore, ChatService chatService) {
        super(name, description);
        requirePermission(ignore ? TSEssentialsPermissions.IGNORE : TSEssentialsPermissions.UNIGNORE);
        this.chatService = chatService;
        this.ignore = ignore;
        this.usernameArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        String username = context.get(usernameArg);
        PlayerRef target = PlayerLookup.findPlayerByName(username).orElse(null);
        if (target == null) {
            player.sendMessage(Message.raw("Player not found: " + username).color("#FF6B6B"));
        } else if (ignore) {
            chatService.ignore(player, target);
        } else {
            chatService.unignore(player, target);
        }
        return CompletableFuture.completedFuture(null);
    }
}
