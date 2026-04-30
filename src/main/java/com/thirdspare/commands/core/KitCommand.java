package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.kits.KitService;
import com.thirdspare.core.kits.data.KitDefinition;
import com.thirdspare.core.kits.ui.KitSelectionPage;
import com.thirdspare.core.ui.CorePageOpener;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KitCommand extends AbstractCommand {
    private final KitService service;
    private final OptionalArg<String> kitArg;

    public KitCommand(KitService service) {
        super("kit", "Claim a kit");
        requirePermission(TSEssentialsPermissions.KIT);
        this.service = service;
        this.kitArg = withOptionalArg("kit", "Kit name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        String kitName = context.get(kitArg);
        if (kitName == null || kitName.isBlank()) {
            CorePageOpener.open(player, new KitSelectionPage(player, service));
            List<KitDefinition> kits = service.accessibleKits(player);
            if (kits.isEmpty()) {
                player.sendMessage(Message.raw("No kits are available to you.").color("#BBBBBB"));
            } else {
                player.sendMessage(Message.raw("Available kits: " + String.join(", ", kits.stream().map(KitDefinition::getName).toList())).color("#F1BA50"));
            }
            return CompletableFuture.completedFuture(null);
        }

        CommandUtils.runOnPlayerWorld(context, player, scheduledPlayer -> {
            KitService.GrantResult result = service.grant(scheduledPlayer, kitName);
            scheduledPlayer.sendMessage(Message.raw(result.message()).color(result.success() ? "#8DE969" : "#FF6B6B"));
        });
        return CompletableFuture.completedFuture(null);
    }
}
