package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.kits.KitService;
import com.thirdspare.core.kits.data.KitDefinition;
import com.thirdspare.core.kits.ui.KitAdminPage;
import com.thirdspare.core.ui.CorePageOpener;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class KitAdminCommand extends AbstractCommand {
    private final KitService service;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> kitArg;
    private final OptionalArg<String> valueArg;

    public KitAdminCommand(KitService service) {
        super("kitadmin", "Manage kits");
        requirePermission(TSEssentialsPermissions.KIT_ADMIN);
        this.service = service;
        this.actionArg = withOptionalArg("action", "list, create, delete, capture, clear, enable, disable", ArgTypes.STRING);
        this.kitArg = withOptionalArg("kit", "Kit name", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "Value", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String action = context.get(actionArg);
        if (action == null || action.isBlank() || action.equalsIgnoreCase("list")) {
            PlayerRef player = CommandUtils.getPlayerFromContext(context, false);
            if (player != null) {
                CorePageOpener.open(player, new KitAdminPage(player, service));
            }
            context.sendMessage(Message.raw("Kits: " + String.join(", ", service.getManager().getKits().stream().map(KitDefinition::getName).toList())).color("#F1BA50"));
            return CompletableFuture.completedFuture(null);
        }
        String kit = context.get(kitArg);
        String value = context.get(valueArg);
        String error = switch (action.toLowerCase()) {
            case "create" -> service.getManager().createKit(kit);
            case "delete" -> service.getManager().deleteKit(kit);
            case "clear" -> service.getManager().clearItems(kit);
            case "enable" -> updateEnabled(kit, true);
            case "disable" -> updateEnabled(kit, false);
            case "capture" -> capture(context, kit);
            case "permission" -> updatePermission(kit, value);
            default -> "Usage: /kitadmin [list|create|delete|capture|clear|enable|disable|permission]";
        };
        context.sendMessage(Message.raw(error == null ? "Kit updated." : error).color(error == null ? "#8DE969" : "#FF6B6B"));
        return CompletableFuture.completedFuture(null);
    }

    private String updateEnabled(String kitName, boolean enabled) {
        KitDefinition kit = service.getManager().getKit(kitName);
        if (kit == null) {
            return "Unknown kit: " + kitName;
        }
        return service.getManager().updateKit(kit.getName(), kit.getDisplayName(), kit.getDescription(), kit.getPermission(), kit.getCooldownSeconds(), enabled);
    }

    private String updatePermission(String kitName, String permission) {
        KitDefinition kit = service.getManager().getKit(kitName);
        if (kit == null) {
            return "Unknown kit: " + kitName;
        }
        return service.getManager().updateKit(kit.getName(), kit.getDisplayName(), kit.getDescription(), permission, kit.getCooldownSeconds(), kit.isEnabled());
    }

    private String capture(CommandContext context, String kitName) {
        PlayerRef playerRef = CommandUtils.getPlayerFromContext(context, true);
        if (playerRef == null) {
            return "This command can only be used by players.";
        }
        Player player = playerRef.getComponent(Player.getComponentType());
        if (player == null) {
            return "Unable to access your inventory.";
        }
        return service.getManager().captureKit(kitName, player.getInventory());
    }
}
