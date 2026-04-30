package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.nearby.NearbyService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NearbyCommand extends AbstractCommand {
    private final NearbyService service;

    public NearbyCommand(NearbyService service) {
        super("nearby", "List nearby players");
        requirePermission(TSEssentialsPermissions.NEARBY);
        this.service = service;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        List<NearbyService.NearbyPlayer> nearby = service.findNearby(player);
        if (nearby.isEmpty()) {
            player.sendMessage(Message.raw("No players are nearby.").color("#BBBBBB"));
        } else {
            player.sendMessage(Message.raw("Nearby players:").color("#F1BA50"));
            for (NearbyService.NearbyPlayer entry : nearby) {
                player.sendMessage(Message.raw(entry.username() + " - " + Math.round(entry.distance()) + " blocks").color("#FFFFFF"));
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
