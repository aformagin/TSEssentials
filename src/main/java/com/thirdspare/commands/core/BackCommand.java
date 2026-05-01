package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.back.BackService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class BackCommand extends AbstractCommand {
    private final BackService service;

    public BackCommand(BackService service) {
        super("back", "Return to your previous location");
        requirePermission(TSEssentialsPermissions.BACK);
        this.service = service;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        CommandUtils.runOnPlayerWorld(context, player, scheduledPlayer -> {
            BackService.BackResult result = service.teleportBack(scheduledPlayer);
            scheduledPlayer.sendMessage(Message.raw(result.message()).color(result.success() ? "#8DE969" : "#FF6B6B"));
        });
        return CompletableFuture.completedFuture(null);
    }
}
