package com.thirdspare.modules.claims.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ClaimBypassCommand extends AbstractCommand {
    private final ClaimsService claimsService;

    public ClaimBypassCommand(ClaimsService claimsService) {
        super("claimbypass", "Toggle claim bypass mode");
        this.claimsService = claimsService;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) return CompletableFuture.completedFuture(null);

        if (!claimsService.isAdmin(player)) {
            player.sendMessage(Message.raw("You do not have permission to bypass claims.").color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }
        boolean enabled = claimsService.toggleBypass(player);
        player.sendMessage(Message.raw("Claim bypass " + (enabled ? "enabled." : "disabled."))
                .color(enabled ? "#8DE969" : "#FFB347"));
        return CompletableFuture.completedFuture(null);
    }
}
