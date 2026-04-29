package com.thirdspare.modules.permissions.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.thirdspare.modules.permissions.PermissionsManager;
import com.thirdspare.modules.permissions.PermissionsService;
import com.thirdspare.modules.permissions.TSEPermissionsNodes;
import com.thirdspare.modules.permissions.data.PermissionsGroup;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class PermissionsCommand extends AbstractCommand {
    private final PermissionsService service;
    private final OptionalArg<String> areaArg;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> targetArg;
    private final OptionalArg<String> valueArg;
    private final OptionalArg<String> extraArg;

    public PermissionsCommand(PermissionsService service) {
        super("tsperm", "Manage TSEssentials permissions");
        requirePermission(TSEPermissionsNodes.UI);
        this.service = service;
        this.areaArg = withOptionalArg("area", "group, user, test, reload", ArgTypes.STRING);
        this.actionArg = withOptionalArg("action", "Action", ArgTypes.STRING);
        this.targetArg = withOptionalArg("target", "Group, player, or UUID", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "Value", ArgTypes.STRING);
        this.extraArg = withOptionalArg("extra", "Extra value", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String area = lower(context.get(areaArg));
        if (area.isBlank()) {
            PermissionsUICommand.openPage(context, service);
            return CompletableFuture.completedFuture(null);
        }

        switch (area) {
            case "group" -> handleGroup(context);
            case "user" -> handleUser(context);
            case "test" -> handleTest(context);
            case "reload" -> handleReload(context);
            default -> context.sendMessage(error("Usage: /tsperm [group|user|test|reload]"));
        }
        return CompletableFuture.completedFuture(null);
    }

    private void handleGroup(CommandContext context) {
        String action = lower(context.get(actionArg));
        String group = context.get(targetArg);
        String value = context.get(valueArg);

        switch (action) {
            case "list" -> {
                Message message = Message.raw("Groups: ").color("#FFFFFF");
                for (PermissionsGroup permissionsGroup : service.listGroups()) {
                    message.insert(Message.raw(permissionsGroup.getName() + " ").color("#8DE969"));
                }
                context.sendMessage(message);
            }
            case "create" -> sendResult(context, TSEPermissionsNodes.GROUP_CREATE, service.createGroup(group, value));
            case "delete" -> sendResult(context, TSEPermissionsNodes.GROUP_DELETE, service.deleteGroup(group));
            case "addnode" -> sendResult(context, TSEPermissionsNodes.GROUP_ADD_NODE, service.addNode(group, value));
            case "removenode" -> sendResult(context, TSEPermissionsNodes.GROUP_REMOVE_NODE, service.removeNode(group, value));
            default -> context.sendMessage(error("Usage: /tsperm group [list|create|delete|addnode|removenode]"));
        }
    }

    private void handleUser(CommandContext context) {
        String action = lower(context.get(actionArg));
        String target = context.get(targetArg);
        String group = context.get(valueArg);

        switch (action) {
            case "groups" -> {
                if (!hasPermission(context, TSEPermissionsNodes.USER_GROUPS)) {
                    return;
                }
                service.getTargetView(target).ifPresentOrElse(
                        view -> context.sendMessage(success(view.displayName() + " groups: " + String.join(", ", view.groups()))),
                        () -> context.sendMessage(error("Unknown player. Use an online name, known username, or UUID."))
                );
            }
            case "addgroup" -> sendResult(context, TSEPermissionsNodes.USER_ADD_GROUP, service.addUserToGroup(target, group));
            case "removegroup" -> sendResult(context, TSEPermissionsNodes.USER_REMOVE_GROUP, service.removeUserFromGroup(target, group));
            default -> context.sendMessage(error("Usage: /tsperm user [groups|addgroup|removegroup]"));
        }
    }

    private void handleTest(CommandContext context) {
        if (!hasPermission(context, TSEPermissionsNodes.TEST)) {
            return;
        }
        String target = context.get(actionArg);
        String node = context.get(targetArg);
        service.getTargetView(target).ifPresentOrElse(
                view -> context.sendMessage(success(view.displayName() + " " +
                        (service.hasPermission(view.uuid(), node) ? "has " : "does not have ") + node + ".")),
                () -> context.sendMessage(error("Unknown player. Use an online name, known username, or UUID."))
        );
    }

    private void handleReload(CommandContext context) {
        if (!hasPermission(context, TSEPermissionsNodes.RELOAD)) {
            return;
        }
        service.reload();
        context.sendMessage(success("Permissions configs reloaded."));
    }

    private void sendResult(CommandContext context, String permission, PermissionsManager.MutationResult result) {
        if (!hasPermission(context, permission)) {
            return;
        }
        context.sendMessage(result.success() ? success(result.message()) : error(result.message()));
    }

    private boolean hasPermission(CommandContext context, String permission) {
        var player = CommandUtils.getPlayerFromContext(context, false);
        if (player != null && !CommandUtils.hasPermission(player, permission)) {
            player.sendMessage(error("You do not have permission for this permissions action."));
            return false;
        }
        return true;
    }

    private static String lower(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static Message success(String value) {
        return Message.raw(value).color("#8DE969");
    }

    private static Message error(String value) {
        return Message.raw(value).color("#FF6B6B");
    }
}
