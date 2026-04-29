package com.thirdspare.commands.economy;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.economy.EconomyManager;
import com.thirdspare.economy.EconomyService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class EcoCommand extends AbstractCommand {
    private final EconomyService economyService;
    private final EconomyManager economyManager;
    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> amountArg;

    public EcoCommand(EconomyService economyService) {
        super("eco", "Admin economy controls");
        requirePermission(TSEssentialsPermissions.ECO);
        this.economyService = economyService;
        this.economyManager = economyService.getEconomyManager();
        this.actionArg = withRequiredArg("action", "give, take, or set", ArgTypes.STRING);
        this.playerArg = withRequiredArg("player", "Known player name or UUID", ArgTypes.STRING);
        this.amountArg = withRequiredArg("amount", "Amount", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String action = context.get(actionArg).toLowerCase();
        String permission = switch (action) {
            case "give" -> TSEssentialsPermissions.ECO_GIVE;
            case "take" -> TSEssentialsPermissions.ECO_TAKE;
            case "set" -> TSEssentialsPermissions.ECO_SET;
            default -> null;
        };

        if (permission == null) {
            context.sendMessage(Message.raw("Unknown action. Use give, take, or set.").color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef sender = CommandUtils.getPlayerFromContext(context, false);
        if (sender != null && !CommandUtils.hasPermission(sender, permission)) {
            sender.sendMessage(Message.raw("You do not have permission to use /eco " + action + ".").color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        EconomyManager.ParseResult amount = economyManager.parseAmount(context.get(amountArg));
        if (!amount.success()) {
            context.sendMessage(Message.raw("Invalid amount: " + amount.errorMessage()).color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        String target = context.get(playerArg);
        EconomyService.MutationResult result = switch (action) {
            case "give" -> economyService.deposit(target, amount.amount());
            case "take" -> economyService.withdraw(target, amount.amount());
            case "set" -> economyService.setBalance(target, amount.amount());
            default -> throw new IllegalStateException("Unexpected action: " + action);
        };

        if (!result.success()) {
            context.sendMessage(Message.raw(result.error()).color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        String formattedAmount = economyManager.format(amount.amount());
        String message = switch (action) {
            case "give" -> "Gave " + result.account().displayName() + " " + formattedAmount + ".";
            case "take" -> "Took " + formattedAmount + " from " + result.account().displayName() + ".";
            case "set" -> "Set " + result.account().displayName() + "'s balance to " + economyManager.format(result.account().balance()) + ".";
            default -> "";
        };
        context.sendMessage(Message.raw(message).color("#8DE969"));
        return CompletableFuture.completedFuture(null);
    }
}
