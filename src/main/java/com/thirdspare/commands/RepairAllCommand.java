package com.thirdspare.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class RepairAllCommand extends AbstractCommand {
    public RepairAllCommand() {
        super("repairall", "Repair all items in your inventory");
        requirePermission(TSEssentialsPermissions.REPAIR_ALL);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        CommandUtils.runOnPlayerWorld(context, player, scheduledPlayer -> {
            int repaired = ItemRepairSupport.repairAll(scheduledPlayer);
            if (repaired == 0) {
                scheduledPlayer.sendMessage(Message.raw("No repairable items were found.").color("#FF6B6B"));
            } else {
                scheduledPlayer.sendMessage(Message.raw("Repaired " + repaired + " item(s).").color("#8DE969"));
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
