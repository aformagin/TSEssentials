package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.thirdspare.core.motd.MotdManager;
import com.thirdspare.core.motd.ui.MotdAdminPage;
import com.thirdspare.core.ui.CorePageOpener;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MotdAdminCommand extends AbstractCommand {
    private final MotdManager manager;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> valueArg;

    public MotdAdminCommand(MotdManager manager) {
        super("motdadmin", "Manage MOTD");
        requirePermission(TSEssentialsPermissions.MOTD_ADMIN);
        this.manager = manager;
        this.actionArg = withOptionalArg("action", "show, enable, disable, joinon, joinoff, set", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "MOTD lines separated by |", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String action = context.get(actionArg);
        if (action == null || action.isBlank() || action.equalsIgnoreCase("show")) {
            var player = CommandUtils.getPlayerFromContext(context, false);
            if (player != null) {
                CorePageOpener.open(player, new MotdAdminPage(player, manager));
            }
            context.sendMessage(Message.raw("MOTD: " + String.join(" | ", manager.getMotd().getLines())).color("#F1BA50"));
            return CompletableFuture.completedFuture(null);
        }
        switch (action.toLowerCase()) {
            case "enable" -> manager.getMotd().setEnabled(true);
            case "disable" -> manager.getMotd().setEnabled(false);
            case "joinon" -> manager.getMotd().setShowOnJoin(true);
            case "joinoff" -> manager.getMotd().setShowOnJoin(false);
            case "set" -> manager.getMotd().setLines((context.get(valueArg) != null ? context.get(valueArg) : "").replace("|", "\n").lines().toList());
            default -> {
                context.sendMessage(Message.raw("Usage: /motdadmin [show|enable|disable|joinon|joinoff|set]").color("#FF6B6B"));
                return CompletableFuture.completedFuture(null);
            }
        }
        manager.save();
        context.sendMessage(Message.raw("MOTD updated.").color("#8DE969"));
        return CompletableFuture.completedFuture(null);
    }
}
