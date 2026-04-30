package com.thirdspare.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HealCommand extends AbstractCommand {
    private final OptionalArg<String> playerArg;

    public HealCommand() {
        super("heal", "Restore health to full");
        requirePermission(TSEssentialsPermissions.HEAL);
        this.playerArg = withOptionalArg("player", "Player to heal", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String targetName = context.get(playerArg);
        PlayerRef sender = CommandUtils.getPlayerFromContext(context, targetName == null || targetName.isBlank());
        PlayerRef target = sender;

        if (targetName != null && !targetName.isBlank()) {
            Optional<PlayerRef> found = PlayerLookup.findPlayerByName(targetName);
            if (found.isEmpty()) {
                context.sendMessage(Message.raw("Player not found: " + targetName).color("#FF6B6B"));
                return CompletableFuture.completedFuture(null);
            }
            target = found.get();
            if (!isSelf(sender, target) && !hasOthersPermission(context, sender)) {
                context.sendMessage(Message.raw("You do not have permission to heal other players.").color("#FF6B6B"));
                return CompletableFuture.completedFuture(null);
            }
        }

        if (target == null) {
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef finalTarget = target;
        CommandUtils.runOnPlayerWorld(context, finalTarget, scheduledTarget -> {
            EntityStatMap stats = scheduledTarget.getComponent(EntityStatMap.getComponentType());
            if (stats == null) {
                context.sendMessage(Message.raw("Unable to access health for " + scheduledTarget.getUsername() + ".").color("#FF6B6B"));
                return;
            }

            stats.maximizeStatValue(DefaultEntityStatTypes.getHealth());
            scheduledTarget.sendMessage(Message.raw("You have been healed.").color("#8DE969"));
            if (!isSelf(sender, scheduledTarget)) {
                context.sendMessage(Message.raw("Healed " + scheduledTarget.getUsername() + ".").color("#8DE969"));
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    private boolean hasOthersPermission(CommandContext context, PlayerRef sender) {
        if (sender == null) {
            return true;
        }
        return CommandUtils.hasPermission(sender, TSEssentialsPermissions.HEAL_OTHERS);
    }

    private boolean isSelf(PlayerRef sender, PlayerRef target) {
        return sender != null && target != null && sender.getUuid().equals(target.getUuid());
    }
}
