package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.motd.MotdManager;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MotdCommand extends AbstractCommand {
    private final MotdManager manager;

    public MotdCommand(MotdManager manager) {
        super("motd", "Show the message of the day");
        requirePermission(TSEssentialsPermissions.MOTD);
        this.manager = manager;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player != null) {
            manager.sendTo(player, true);
        }
        return CompletableFuture.completedFuture(null);
    }
}
