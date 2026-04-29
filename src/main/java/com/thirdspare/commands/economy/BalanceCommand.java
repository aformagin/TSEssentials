package com.thirdspare.commands.economy;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.economy.EconomyManager;
import com.thirdspare.economy.EconomyService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class BalanceCommand extends AbstractCommand {
    private final EconomyService economyService;
    private final EconomyManager economyManager;
    private final OptionalArg<String> playerArg;

    public BalanceCommand(EconomyService economyService) {
        super("bal", "View economy balances");
        requirePermission(TSEssentialsPermissions.BALANCE);
        this.economyService = economyService;
        this.economyManager = economyService.getEconomyManager();
        this.playerArg = withOptionalArg("player", "Known player name or UUID", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String target = context.get(playerArg);
        if (target == null || target.isBlank()) {
            PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
            if (player != null) {
                var account = economyService.getAccountView(player);
                player.sendMessage(Message.raw("Balance: " + economyManager.format(account.balance())).color("#8DE969"));
            }
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef player = CommandUtils.getPlayerFromContext(context, false);
        if (player != null && !CommandUtils.hasPermission(player, TSEssentialsPermissions.BALANCE_OTHERS)) {
            player.sendMessage(Message.raw("You do not have permission to view other players' balances.").color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        var account = economyService.getAccountView(target);
        if (account == null) {
            context.sendMessage(Message.raw("Unknown player: " + target).color("#FF6B6B"));
        } else {
            context.sendMessage(Message.raw(account.displayName() + "'s balance: " + economyManager.format(account.balance())).color("#8DE969"));
        }
        return CompletableFuture.completedFuture(null);
    }
}
