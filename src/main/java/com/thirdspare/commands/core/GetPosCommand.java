package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.position.PositionService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class GetPosCommand extends AbstractCommand {
    private final PositionService service;
    private final OptionalArg<String> playerArg;

    public GetPosCommand(PositionService service) {
        super("getpos", "Show player coordinates");
        requirePermission(TSEssentialsPermissions.GET_POS);
        this.service = service;
        this.playerArg = withOptionalArg("player", "Player to inspect", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String targetName = context.get(playerArg);
        PlayerRef sender = CommandUtils.getPlayerFromContext(context, targetName == null || targetName.isBlank());
        PlayerRef target = sender;
        if (targetName != null && !targetName.isBlank()) {
            if (sender != null && !CommandUtils.hasPermission(sender, TSEssentialsPermissions.GET_POS_OTHERS)) {
                context.sendMessage(Message.raw("You do not have permission to inspect other players.").color("#FF6B6B"));
                return CompletableFuture.completedFuture(null);
            }
            target = PlayerLookup.findPlayerByName(targetName).orElse(null);
            if (target == null) {
                context.sendMessage(Message.raw("Player not found: " + targetName).color("#FF6B6B"));
                return CompletableFuture.completedFuture(null);
            }
        }
        if (target != null) {
            context.sendMessage(Message.raw(service.formatPosition(target)).color("#FFFFFF"));
        }
        return CompletableFuture.completedFuture(null);
    }
}
