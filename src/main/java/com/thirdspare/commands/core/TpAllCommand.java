package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.teleport.TeleportAllService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TpAllCommand extends AbstractCommand {
    private final TeleportAllService service;

    public TpAllCommand(TeleportAllService service) {
        super("tpall", "Teleport all players to you");
        requirePermission(TSEssentialsPermissions.TP_ALL);
        this.service = service;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        int teleported = service.teleportAllTo(player);
        player.sendMessage(Message.raw("Teleported " + teleported + " player(s) to you.").color("#8DE969"));
        return CompletableFuture.completedFuture(null);
    }
}
