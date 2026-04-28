package com.thirdspare.commands.claims;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.thirdspare.claims.ClaimCorner;
import com.thirdspare.claims.ClaimManager;
import com.thirdspare.data.claims.Claim;
import com.thirdspare.ui.ClaimMembersPage;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClaimCommand extends AbstractCommand {
    private final ClaimManager claimManager;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> nameArg;
    private final OptionalArg<String> valueArg;

    public ClaimCommand(ClaimManager claimManager) {
        super("claim", "Manage land claims");
        this.claimManager = claimManager;
        this.actionArg = withOptionalArg("action", "pos1, pos2, create, list, info, delete, trust, untrust, members, bypass", ArgTypes.STRING);
        this.nameArg = withOptionalArg("name", "Claim or player name", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "Optional claim name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }

        String action = context.get(actionArg);
        if (action == null || action.isBlank() || action.equalsIgnoreCase("list")) {
            listClaims(player);
            return CompletableFuture.completedFuture(null);
        }

        String name = context.get(nameArg);
        String value = context.get(valueArg);
        String error = null;

        switch (action.toLowerCase()) {
            case "pos1" -> setCorner(player, 1);
            case "pos2" -> setCorner(player, 2);
            case "create" -> error = create(player, name);
            case "info" -> sendInfo(player, claimManager.resolveClaim(player, name));
            case "delete" -> error = claimManager.deleteClaim(player, claimManager.resolveClaim(player, name));
            case "trust" -> error = trust(player, name, value);
            case "untrust" -> error = untrust(player, name, value);
            case "members" -> openMembersPage(player, claimManager.resolveClaim(player, name));
            case "bypass" -> error = toggleBypass(player);
            default -> error = "Usage: /claim [pos1|pos2|create|list|info|delete|trust|untrust|members|bypass]";
        }

        if (error != null) {
            player.sendMessage(Message.raw(error).color("#FF6B6B"));
        }
        return CompletableFuture.completedFuture(null);
    }

    private void setCorner(PlayerRef player, int corner) {
        var position = player.getTransform().getPosition();
        ClaimCorner selection = new ClaimCorner(
                player.getWorldUuid().toString(),
                (int) Math.floor(position.x()),
                (int) Math.floor(position.y()),
                (int) Math.floor(position.z())
        );
        claimManager.setCorner(player, corner, selection);
        player.sendMessage(Message.raw("Claim position " + corner + " set to "
                + selection.x() + ", " + selection.y() + ", " + selection.z() + ".").color("#8DE969"));
    }

    private String create(PlayerRef player, String name) {
        if (name == null || name.isBlank()) {
            return "Usage: /claim create <name>";
        }
        String error = claimManager.createClaim(player, name);
        if (error == null) {
            player.sendMessage(Message.raw("Created claim " + Claim.normalizeName(name) + ".").color("#8DE969"));
        }
        return error;
    }

    private void listClaims(PlayerRef player) {
        var claims = claimManager.getManageableClaims(player);
        if (claims.isEmpty()) {
            player.sendMessage(Message.raw("You do not manage any claims.").color("#FFB347"));
            return;
        }
        Message message = Message.raw("Claims: ").color("#FFFFFF");
        for (Claim claim : claims) {
            message.insert(Message.raw(claim.getName() + " ").color("#8DE969"));
        }
        player.sendMessage(message);
    }

    private void sendInfo(PlayerRef player, Claim claim) {
        if (claim == null) {
            player.sendMessage(Message.raw("No claim found.").color("#FF6B6B"));
            return;
        }
        player.sendMessage(Message.raw("Claim " + claim.getName()
                + " owned by " + claim.getOwnerName()
                + " with " + claim.getTrustedUuids().size() + " trusted member(s). "
                + claim.getRegion().describeBounds()).color("#FFFFFF"));
    }

    private String trust(PlayerRef actor, String playerName, String claimName) {
        if (playerName == null || playerName.isBlank()) {
            return "Usage: /claim trust <player> [claim]";
        }
        PlayerRef target = PlayerLookup.findPlayerByName(playerName).orElse(null);
        if (target == null) {
            return "Player must be online to trust them.";
        }
        Claim claim = claimManager.resolveClaim(actor, claimName);
        String error = claimManager.trust(actor, claim, target);
        if (error == null) {
            actor.sendMessage(Message.raw("Trusted " + target.getUsername() + " on " + claim.getName() + ".").color("#8DE969"));
        }
        return error;
    }

    private String untrust(PlayerRef actor, String playerName, String claimName) {
        if (playerName == null || playerName.isBlank()) {
            return "Usage: /claim untrust <player> [claim]";
        }
        Claim claim = claimManager.resolveClaim(actor, claimName);
        if (claim == null) {
            return "Unknown claim.";
        }
        UUID targetUuid = PlayerLookup.findPlayerByName(playerName)
                .map(PlayerRef::getUuid)
                .orElseGet(() -> findTrustedUuidByName(claim, playerName));
        if (targetUuid == null) {
            return "That trusted player was not found.";
        }
        String error = claimManager.untrust(actor, claim, targetUuid);
        if (error == null) {
            actor.sendMessage(Message.raw("Removed trusted member from " + claim.getName() + ".").color("#8DE969"));
        }
        return error;
    }

    private UUID findTrustedUuidByName(Claim claim, String playerName) {
        for (UUID uuid : claim.getTrustedUuids()) {
            if (claim.displayMember(uuid).equalsIgnoreCase(playerName) || uuid.toString().equalsIgnoreCase(playerName)) {
                return uuid;
            }
        }
        return null;
    }

    private String toggleBypass(PlayerRef player) {
        if (!claimManager.isAdmin(player)) {
            return "You do not have permission to bypass claims.";
        }
        boolean enabled = claimManager.toggleBypass(player);
        player.sendMessage(Message.raw("Claim bypass " + (enabled ? "enabled." : "disabled."))
                .color(enabled ? "#8DE969" : "#FFB347"));
        return null;
    }

    private void openMembersPage(PlayerRef player, Claim selectedClaim) {
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                var ref = player.getReference();
                var store = ref.getStore();
                Player playerComponent = store.getComponent(ref, Player.getComponentType());
                if (playerComponent != null) {
                    playerComponent.getPageManager().openCustomPage(ref, store,
                            new ClaimMembersPage(player, claimManager, selectedClaim != null ? selectedClaim.getId() : null));
                }
            } catch (IllegalStateException ignored) {
                // The player can leave before the queued UI open runs.
            }
        });
    }
}
