package com.thirdspare.modules.claims.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClaimCommand extends AbstractCommand {
    private static final String COLOR_SUCCESS = "#8DE969";
    private static final String COLOR_ERROR = "#FF6B6B";
    private static final String COLOR_INFO = "#FFFFFF";
    private static final String COLOR_WARN = "#FFB347";

    private final ClaimsService claimsService;
    private final TSEUiDocument membersUi;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> nameArg;
    private final OptionalArg<String> valueArg;

    public ClaimCommand(ClaimsService claimsService, TSEUiDocument membersUi) {
        super("claim", "Manage land claims");
        this.claimsService = claimsService;
        this.membersUi = membersUi;
        this.actionArg = withOptionalArg("action",
                "pos1, pos2, clear, create, list, info, delete, trust, untrust, members, bypass",
                ArgTypes.STRING);
        this.nameArg = withOptionalArg("name", "Claim or player name", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "Optional claim name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) return CompletableFuture.completedFuture(null);

        String action = context.get(actionArg);
        String name = context.get(nameArg);
        String value = context.get(valueArg);

        if (action == null || action.isBlank() || action.equalsIgnoreCase("list")) {
            listClaims(player);
            return CompletableFuture.completedFuture(null);
        }

        String error = switch (action.toLowerCase()) {
            case "pos1" -> setCorner(player, 1);
            case "pos2" -> setCorner(player, 2);
            case "clear" -> { clearSelection(player); yield null; }
            case "create" -> createClaim(player, name);
            case "info" -> { showInfo(player, name); yield null; }
            case "delete" -> deleteClaim(player, name);
            case "trust" -> trust(player, name, value);
            case "untrust" -> untrust(player, name, value);
            case "members" -> { openMembersPage(player, name); yield null; }
            case "bypass" -> toggleBypass(player);
            default -> "Usage: /claim [pos1|pos2|clear|create|list|info|delete|trust|untrust|members|bypass]";
        };

        if (error != null) {
            player.sendMessage(Message.raw(error).color(COLOR_ERROR));
        }
        return CompletableFuture.completedFuture(null);
    }

    private String setCorner(PlayerRef player, int corner) {
        var pos = player.getTransform().getPosition();
        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);
        if (corner == 1) {
            claimsService.selectionService().setPos1(
                    player.getUuid(), player.getWorldUuid(), x, y, z);
        } else {
            claimsService.selectionService().setPos2(
                    player.getUuid(), player.getWorldUuid(), x, y, z);
        }
        player.sendMessage(Message.raw(
                "Claim corner " + corner + " set to " + x + ", " + y + ", " + z + "."
        ).color(COLOR_SUCCESS));
        return null;
    }

    private void clearSelection(PlayerRef player) {
        claimsService.selectionService().clear(player.getUuid());
        player.sendMessage(Message.raw("Claim selection cleared.").color(COLOR_WARN));
    }

    private String createClaim(PlayerRef player, String name) {
        if (name == null || name.isBlank()) {
            return "Usage: /claim create <name>";
        }
        String error = claimsService.createClaimFromSelection(player, name);
        if (error == null) {
            player.sendMessage(Message.raw(
                    "Created claim '" + ClaimDefinition.normalizeName(name) + "'."
            ).color(COLOR_SUCCESS));
        }
        return error;
    }

    private void listClaims(PlayerRef player) {
        var claims = claimsService.getClaimsFor(player);
        if (claims.isEmpty()) {
            player.sendMessage(Message.raw("You do not manage any claims.").color(COLOR_WARN));
            return;
        }
        Message msg = Message.raw("Your claims: ").color(COLOR_INFO);
        for (ClaimDefinition claim : claims) {
            msg.insert(Message.raw(claim.getDisplayName() + " ").color(COLOR_SUCCESS));
        }
        player.sendMessage(msg);
    }

    private void showInfo(PlayerRef player, String name) {
        Optional<ClaimDefinition> opt = claimsService.resolveClaim(player, name);
        if (opt.isEmpty()) {
            player.sendMessage(Message.raw("No claim found here.").color(COLOR_ERROR));
            return;
        }
        ClaimDefinition claim = opt.get();
        player.sendMessage(Message.raw(
                "Claim '" + claim.getDisplayName() + "' owned by " + claim.getOwnerName()
                        + " | " + claim.getTrustedUuids().size() + " trusted | "
                        + claim.getBounds().describeBounds()
        ).color(COLOR_INFO));
    }

    private String deleteClaim(PlayerRef player, String name) {
        Optional<ClaimDefinition> opt = claimsService.resolveClaim(player, name);
        if (opt.isEmpty()) return "No claim found.";
        String error = claimsService.deleteClaim(player, opt.get().getId());
        if (error == null) {
            player.sendMessage(Message.raw(
                    "Deleted claim '" + opt.get().getDisplayName() + "'."
            ).color(COLOR_SUCCESS));
        }
        return error;
    }

    private String trust(PlayerRef actor, String playerName, String claimName) {
        if (playerName == null || playerName.isBlank()) {
            return "Usage: /claim trust <player> [claim]";
        }
        PlayerRef target = PlayerLookup.findPlayerByName(playerName).orElse(null);
        if (target == null) return "Player must be online to trust them.";

        Optional<ClaimDefinition> opt = claimsService.resolveClaim(actor, claimName);
        if (opt.isEmpty()) return "No claim found.";

        String error = claimsService.trust(actor, opt.get().getId(),
                target.getUuid(), target.getUsername());
        if (error == null) {
            actor.sendMessage(Message.raw(
                    "Trusted " + target.getUsername() + " on '" + opt.get().getDisplayName() + "'."
            ).color(COLOR_SUCCESS));
        }
        return error;
    }

    private String untrust(PlayerRef actor, String playerName, String claimName) {
        if (playerName == null || playerName.isBlank()) {
            return "Usage: /claim untrust <player> [claim]";
        }
        Optional<ClaimDefinition> opt = claimsService.resolveClaim(actor, claimName);
        if (opt.isEmpty()) return "No claim found.";

        ClaimDefinition claim = opt.get();
        var targetUuidOpt = PlayerLookup.findPlayerByName(playerName)
                .map(PlayerRef::getUuid);
        var targetUuid = targetUuidOpt.orElseGet(() ->
                findTrustedByName(claim, playerName));
        if (targetUuid == null) return "That trusted player was not found.";

        String error = claimsService.untrust(actor, claim.getId(), targetUuid);
        if (error == null) {
            actor.sendMessage(Message.raw(
                    "Removed trusted member from '" + claim.getDisplayName() + "'."
            ).color(COLOR_SUCCESS));
        }
        return error;
    }

    private void openMembersPage(PlayerRef player, String claimName) {
        Optional<ClaimDefinition> opt = claimsService.resolveClaim(player, claimName);
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) return;
        String selectedId = opt.map(ClaimDefinition::getId).orElse(null);
        world.execute(() -> {
            if (!player.isValid()) return;
            try {
                var ref = player.getReference();
                var store = ref.getStore();
                Player pc = store.getComponent(ref, Player.getComponentType());
                if (pc != null) {
                    pc.getPageManager().openCustomPage(ref, store,
                            new com.thirdspare.modules.claims.ui.ClaimMembersPage(
                                    player, claimsService, membersUi, selectedId));
                }
            } catch (IllegalStateException ignored) {
            }
        });
    }

    private String toggleBypass(PlayerRef player) {
        if (!claimsService.isAdmin(player)) {
            return "You do not have permission to bypass claims.";
        }
        boolean enabled = claimsService.toggleBypass(player);
        player.sendMessage(Message.raw("Claim bypass " + (enabled ? "enabled." : "disabled."))
                .color(enabled ? COLOR_SUCCESS : COLOR_WARN));
        return null;
    }

    private static java.util.UUID findTrustedByName(ClaimDefinition claim, String name) {
        for (java.util.UUID uuid : claim.getTrustedUuids()) {
            if (claim.displayMember(uuid).equalsIgnoreCase(name)
                    || uuid.toString().equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }
}
