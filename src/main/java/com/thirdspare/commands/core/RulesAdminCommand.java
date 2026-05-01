package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.thirdspare.core.rules.RulesManager;
import com.thirdspare.core.rules.ui.RulesAdminPage;
import com.thirdspare.core.ui.CorePageOpener;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class RulesAdminCommand extends AbstractCommand {
    private final RulesManager manager;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> valueArg;

    public RulesAdminCommand(RulesManager manager) {
        super("rulesadmin", "Manage server rules");
        requirePermission(TSEssentialsPermissions.RULES_ADMIN);
        this.manager = manager;
        this.actionArg = withOptionalArg("action", "show, title, set", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "Rules separated by |", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String action = context.get(actionArg);
        String value = context.get(valueArg);
        if (action == null || action.isBlank() || action.equalsIgnoreCase("show")) {
            var player = CommandUtils.getPlayerFromContext(context, false);
            if (player != null) {
                CorePageOpener.open(player, new RulesAdminPage(player, manager));
            }
            context.sendMessage(Message.raw(manager.getRules().getTitle() + ": " + String.join(" | ", manager.getRules().getRules())).color("#F1BA50"));
            return CompletableFuture.completedFuture(null);
        }
        switch (action.toLowerCase()) {
            case "title" -> manager.getRules().setTitle(value);
            case "set" -> manager.getRules().setRules((value != null ? value : "").replace("|", "\n").lines().toList());
            default -> {
                context.sendMessage(Message.raw("Usage: /rulesadmin [show|title|set]").color("#FF6B6B"));
                return CompletableFuture.completedFuture(null);
            }
        }
        manager.save();
        context.sendMessage(Message.raw("Rules updated.").color("#8DE969"));
        return CompletableFuture.completedFuture(null);
    }
}
