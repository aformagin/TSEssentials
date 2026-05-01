package com.thirdspare.modules.economy.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.economy.EconomyManager;
import com.thirdspare.modules.economy.EconomyService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class PayCommand extends AbstractCommand {
    private final EconomyService economyService;
    private final EconomyManager economyManager;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> amountArg;

    public PayCommand(EconomyService economyService) {
        super("pay", "Pay a known player");
        requirePermission(TSEssentialsPermissions.PAY);
        this.economyService = economyService;
        this.economyManager = economyService.getEconomyManager();
        this.playerArg = withRequiredArg("player", "Known player name or UUID", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount to pay", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef sender = CommandUtils.getPlayerFromContext(context, true);
        if (sender == null) {
            return CompletableFuture.completedFuture(null);
        }

        String recipient = context.get(playerArg);
        EconomyManager.ParseResult amount = economyManager.parseAmount(context.get(amountArg));
        if (!amount.success()) {
            sender.sendMessage(Message.raw("Invalid amount: " + amount.errorMessage()).color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        EconomyService.TransferResult result = economyService.transfer(sender, recipient, amount.amount());
        if (!result.success()) {
            sender.sendMessage(Message.raw(result.error()).color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        String formattedAmount = economyManager.format(amount.amount());
        sender.sendMessage(Message.raw("Paid " + result.recipient().displayName() + " " + formattedAmount + ".").color("#8DE969"));
        PlayerRef recipientRef = result.recipient().onlinePlayer();
        if (recipientRef != null && recipientRef.isValid()) {
            recipientRef.sendMessage(Message.raw(sender.getUsername() + " paid you " + formattedAmount + ".").color("#8DE969"));
        }
        return CompletableFuture.completedFuture(null);
    }
}
