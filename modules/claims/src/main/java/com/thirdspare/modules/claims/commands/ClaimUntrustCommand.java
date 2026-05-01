package com.thirdspare.modules.claims.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClaimUntrustCommand extends AbstractCommand {
    private final ClaimsService claimsService;
    private final RequiredArg<String> playerArg;
    private final OptionalArg<String> claimArg;

    public ClaimUntrustCommand(ClaimsService claimsService) {
        super("claimuntrust", "Remove a trusted player from a claim");
        this.claimsService = claimsService;
        this.playerArg = withRequiredArg("player", "Player to untrust", ArgTypes.STRING);
        this.claimArg = withOptionalArg("claim", "Claim name (defaults to current location)", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef actor = CommandUtils.getPlayerFromContext(context, true);
        if (actor == null) return CompletableFuture.completedFuture(null);

        String playerName = context.get(playerArg);
        String claimName = context.get(claimArg);

        Optional<ClaimDefinition> opt = claimsService.resolveClaim(actor, claimName);
        if (opt.isEmpty()) {
            actor.sendMessage(Message.raw("No claim found.").color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }
        ClaimDefinition claim = opt.get();

        UUID targetUuid = PlayerLookup.findPlayerByName(playerName)
                .map(PlayerRef::getUuid)
                .orElseGet(() -> findTrustedByName(claim, playerName));
        if (targetUuid == null) {
            actor.sendMessage(Message.raw("That trusted player was not found.").color("#FF6B6B"));
            return CompletableFuture.completedFuture(null);
        }

        String error = claimsService.untrust(actor, claim.getId(), targetUuid);
        if (error != null) {
            actor.sendMessage(Message.raw(error).color("#FF6B6B"));
        } else {
            actor.sendMessage(Message.raw(
                    "Removed trusted member from '" + claim.getDisplayName() + "'."
            ).color("#8DE969"));
        }
        return CompletableFuture.completedFuture(null);
    }

    private static UUID findTrustedByName(ClaimDefinition claim, String name) {
        for (UUID uuid : claim.getTrustedUuids()) {
            if (claim.displayMember(uuid).equalsIgnoreCase(name)
                    || uuid.toString().equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }
}
