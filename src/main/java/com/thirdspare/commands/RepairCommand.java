package com.thirdspare.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class RepairCommand extends AbstractCommand {
    public RepairCommand() {
        super("repair", "Repair the item in your hand");
        requirePermission(TSEssentialsPermissions.REPAIR);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        CommandUtils.runOnPlayerWorld(context, player, scheduledPlayer -> {
            if (ItemRepairSupport.repairHeldItem(scheduledPlayer)) {
                scheduledPlayer.sendMessage(Message.raw("Repaired the item in your hand.").color("#8DE969"));
            } else {
                scheduledPlayer.sendMessage(Message.raw("The item in your hand cannot be repaired.").color("#FF6B6B"));
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
