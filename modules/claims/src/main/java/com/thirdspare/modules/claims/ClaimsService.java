package com.thirdspare.modules.claims;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.claims.data.ClaimBounds;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.thirdspare.utils.CommandUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ClaimsService {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_-]+$");

    private final ClaimsManager claimsManager;
    private final ClaimSelectionService selectionService;
    private final ClaimBypassService bypassService;

    public ClaimsService(ClaimsManager claimsManager,
                         ClaimSelectionService selectionService,
                         ClaimBypassService bypassService) {
        this.claimsManager = claimsManager;
        this.selectionService = selectionService;
        this.bypassService = bypassService;
    }

    public boolean canModify(PlayerRef player, UUID worldUuid, int x, int y, int z) {
        Optional<ClaimDefinition> claim = claimsManager.getClaimAt(worldUuid, x, y, z);
        if (claim.isEmpty()) return true;
        return canAccessClaim(player, claim.get());
    }

    public boolean canInteract(PlayerRef player, UUID worldUuid, int x, int y, int z) {
        return canModify(player, worldUuid, x, y, z);
    }

    public Optional<ClaimDefinition> getClaimAt(UUID worldUuid, int x, int y, int z) {
        return claimsManager.getClaimAt(worldUuid, x, y, z);
    }

    public String createClaimFromSelection(PlayerRef owner, String requestedName) {
        String normalized = ClaimDefinition.normalizeName(requestedName);
        if (normalized.isBlank() || !NAME_PATTERN.matcher(normalized).matches()) {
            return "Claim names may only contain letters, numbers, underscores, and hyphens.";
        }
        String selectionError = selectionService.validateSelection(owner.getUuid());
        if (selectionError != null) return selectionError;

        UUID worldUuid = selectionService.getSelectionWorldUuid(owner.getUuid());
        ClaimBounds bounds = selectionService.buildBoundsFor(owner.getUuid());
        String claimId = ClaimDefinition.claimId(owner.getUuid(), normalized);
        if (claimsManager.getClaim(claimId).isPresent()) {
            return "You already have a claim named '" + normalized + "'.";
        }

        ClaimDefinition claim = new ClaimDefinition(
                claimId, normalized, owner.getUuid(), owner.getUsername(), worldUuid, bounds);
        return claimsManager.createClaim(claim);
    }

    public String deleteClaim(PlayerRef requester, String claimId) {
        Optional<ClaimDefinition> opt = claimsManager.getClaim(claimId);
        if (opt.isEmpty()) return "Unknown claim.";
        ClaimDefinition claim = opt.get();
        if (!canManage(requester, claim)) {
            return "Only the claim owner or an admin can delete this claim.";
        }
        return claimsManager.deleteClaim(claimId);
    }

    public String trust(PlayerRef requester, String claimId, UUID targetUuid, String targetName) {
        Optional<ClaimDefinition> opt = claimsManager.getClaim(claimId);
        if (opt.isEmpty()) return "Unknown claim.";
        ClaimDefinition claim = opt.get();
        if (!canManage(requester, claim)) {
            return "Only the claim owner or an admin can edit members.";
        }
        return claimsManager.trust(claimId, targetUuid, targetName);
    }

    public String untrust(PlayerRef requester, String claimId, UUID targetUuid) {
        Optional<ClaimDefinition> opt = claimsManager.getClaim(claimId);
        if (opt.isEmpty()) return "Unknown claim.";
        ClaimDefinition claim = opt.get();
        if (!canManage(requester, claim)) {
            return "Only the claim owner or an admin can edit members.";
        }
        return claimsManager.untrust(claimId, targetUuid);
    }

    public List<ClaimDefinition> getClaimsFor(PlayerRef player) {
        return claimsManager.getClaims().stream()
                .filter(c -> canManage(player, c))
                .sorted(Comparator.comparing(ClaimDefinition::getDisplayName))
                .toList();
    }

    public List<ClaimDefinition> getAllClaims() {
        return claimsManager.getClaims().stream()
                .sorted(Comparator.comparing(ClaimDefinition::getDisplayName))
                .toList();
    }

    public Optional<ClaimDefinition> resolveClaim(PlayerRef player, String nameOrNull) {
        if (nameOrNull != null && !nameOrNull.isBlank()) {
            String normalized = ClaimDefinition.normalizeName(nameOrNull);
            String ownedId = ClaimDefinition.claimId(player.getUuid(), normalized);
            Optional<ClaimDefinition> owned = claimsManager.getClaim(ownedId);
            if (owned.isPresent()) return owned;
            if (isAdmin(player)) {
                return claimsManager.getClaims().stream()
                        .filter(c -> c.getDisplayName().equals(normalized))
                        .findFirst();
            }
            return Optional.empty();
        }
        var pos = player.getTransform().getPosition();
        return claimsManager.getClaimAt(
                player.getWorldUuid(),
                (int) Math.floor(pos.x),
                (int) Math.floor(pos.y),
                (int) Math.floor(pos.z)
        );
    }

    public boolean canManage(PlayerRef player, ClaimDefinition claim) {
        return claim != null && (claim.isOwner(player.getUuid()) || isAdmin(player));
    }

    public boolean isAdmin(PlayerRef player) {
        return player != null && CommandUtils.hasPermission(player, ClaimsPermissionNodes.ADMIN);
    }

    public boolean toggleBypass(PlayerRef player) {
        return bypassService.toggle(player.getUuid());
    }

    public boolean isBypassing(PlayerRef player) {
        return isAdmin(player) && bypassService.isBypassing(player.getUuid());
    }

    public ClaimSelectionService selectionService() {
        return selectionService;
    }

    public ClaimsManager claimsManager() {
        return claimsManager;
    }

    private boolean canAccessClaim(PlayerRef player, ClaimDefinition claim) {
        return claim == null
                || !claim.isEnabled()
                || claim.canAccess(player.getUuid())
                || isBypassing(player)
                || isAdmin(player);
    }
}
