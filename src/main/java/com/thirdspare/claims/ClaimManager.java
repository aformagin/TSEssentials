package com.thirdspare.claims;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.data.claims.Claim;
import com.thirdspare.data.claims.ClaimsConfig;
import com.thirdspare.data.claims.CuboidRegion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ClaimManager {
    public static final String ADMIN_PERMISSION = "tsessentials.claim.admin";

    private static final Pattern CLAIM_NAME_PATTERN = Pattern.compile("^[a-z0-9_-]+$");
    private final Config<ClaimsConfig> config;
    private final ClaimsConfig claims;
    private final Map<UUID, ClaimSelection> selections = new HashMap<>();
    private final Set<UUID> bypassPlayers = new HashSet<>();

    public ClaimManager(Config<ClaimsConfig> config, ClaimsConfig claims) {
        this.config = config;
        this.claims = claims;
    }

    public void setCorner(PlayerRef player, int corner, ClaimCorner selection) {
        ClaimSelection current = selections.computeIfAbsent(player.getUuid(), uuid -> new ClaimSelection());
        if (corner == 1) {
            current.pos1 = selection;
        } else {
            current.pos2 = selection;
        }
    }

    public ClaimSelection getSelection(PlayerRef player) {
        return selections.get(player.getUuid());
    }

    public String createClaim(PlayerRef owner, String name) {
        String normalized = Claim.normalizeName(name);
        if (!isValidClaimName(normalized)) {
            return "Claim names may only contain letters, numbers, underscores, and hyphens.";
        }
        ClaimSelection selection = selections.get(owner.getUuid());
        if (selection == null || selection.pos1 == null || selection.pos2 == null) {
            return "Set both corners first with /claim pos1 and /claim pos2.";
        }
        if (!selection.pos1.worldUUID().equals(selection.pos2.worldUUID())) {
            return "Both claim corners must be in the same world.";
        }
        if (getOwnedClaim(owner.getUuid(), normalized) != null) {
            return "You already have a claim named " + normalized + ".";
        }

        CuboidRegion region = new CuboidRegion(
                selection.pos1.worldUUID(),
                selection.pos1.x(), selection.pos1.y(), selection.pos1.z(),
                selection.pos2.x(), selection.pos2.y(), selection.pos2.z()
        );
        if (region.isZeroVolume()) {
            return "Claims must have width, height, and depth.";
        }
        Claim overlap = findOverlapping(region, null);
        if (overlap != null) {
            return "That region overlaps claim " + overlap.getName() + ".";
        }

        Claim claim = new Claim(Claim.claimId(owner.getUuid(), normalized), normalized,
                owner.getUuid(), owner.getUsername(), region);
        claims.setClaim(claim);
        save();
        return null;
    }

    public String deleteClaim(PlayerRef actor, Claim claim) {
        if (claim == null) {
            return "Unknown claim.";
        }
        if (!canManage(actor, claim)) {
            return "Only the claim owner or an admin can delete this claim.";
        }
        claims.removeClaim(claim.getId());
        save();
        return null;
    }

    public String trust(PlayerRef actor, Claim claim, PlayerRef target) {
        if (claim == null) {
            return "Unknown claim.";
        }
        if (!canManage(actor, claim)) {
            return "Only the claim owner or an admin can edit members.";
        }
        if (claim.isOwner(target.getUuid())) {
            return "The owner already has access.";
        }
        claim.trust(target.getUuid(), target.getUsername());
        save();
        return null;
    }

    public String untrust(PlayerRef actor, Claim claim, UUID targetUuid) {
        if (claim == null) {
            return "Unknown claim.";
        }
        if (!canManage(actor, claim)) {
            return "Only the claim owner or an admin can edit members.";
        }
        if (!claim.untrust(targetUuid)) {
            return "That player is not trusted on this claim.";
        }
        save();
        return null;
    }

    public Claim getOwnedClaim(UUID ownerUuid, String name) {
        return claims.getClaim(Claim.claimId(ownerUuid, Claim.normalizeName(name)));
    }

    public Claim findClaimByNameVisibleTo(PlayerRef player, String name) {
        String normalized = Claim.normalizeName(name);
        Claim owned = getOwnedClaim(player.getUuid(), normalized);
        if (owned != null) {
            return owned;
        }
        if (!isAdmin(player)) {
            return null;
        }
        return getClaims().stream()
                .filter(claim -> claim.getName().equals(normalized))
                .findFirst()
                .orElse(null);
    }

    public Claim resolveClaim(PlayerRef player, String optionalName) {
        if (optionalName != null && !optionalName.isBlank()) {
            return findClaimByNameVisibleTo(player, optionalName);
        }
        var position = player.getTransform().getPosition();
        return findClaimAt(player.getWorldUuid().toString(),
                (int) Math.floor(position.x()),
                (int) Math.floor(position.y()),
                (int) Math.floor(position.z()));
    }

    public Claim findClaimAt(String worldUUID, int x, int y, int z) {
        for (Claim claim : claims.getAllClaims()) {
            if (claim.getRegion().contains(worldUUID, x, y, z)) {
                return claim;
            }
        }
        return null;
    }

    public List<Claim> getClaims() {
        List<Claim> result = new ArrayList<>(claims.getAllClaims());
        result.sort(Comparator.comparing(Claim::getName));
        return result;
    }

    public List<Claim> getManageableClaims(PlayerRef player) {
        return getClaims().stream()
                .filter(claim -> canManage(player, claim))
                .toList();
    }

    public boolean canManage(PlayerRef player, Claim claim) {
        return claim != null && (claim.isOwner(player.getUuid()) || isAdmin(player));
    }

    public boolean canAccess(PlayerRef player, Claim claim) {
        return claim == null || claim.canAccess(player.getUuid()) || isBypassing(player);
    }

    public boolean isAdmin(PlayerRef player) {
        return player != null && player.hasPermission(ADMIN_PERMISSION);
    }

    public boolean toggleBypass(PlayerRef player) {
        UUID uuid = player.getUuid();
        if (bypassPlayers.contains(uuid)) {
            bypassPlayers.remove(uuid);
            return false;
        }
        bypassPlayers.add(uuid);
        return true;
    }

    public boolean isBypassing(PlayerRef player) {
        return isAdmin(player) && bypassPlayers.contains(player.getUuid());
    }

    public CompletableFuture<Void> save() {
        return config.save();
    }

    private Claim findOverlapping(CuboidRegion region, String ignoredClaimId) {
        for (Claim claim : claims.getAllClaims()) {
            if ((ignoredClaimId == null || !ignoredClaimId.equals(claim.getId()))
                    && claim.getRegion().overlaps(region)) {
                return claim;
            }
        }
        return null;
    }

    private static boolean isValidClaimName(String name) {
        return name != null && !name.isBlank() && CLAIM_NAME_PATTERN.matcher(name).matches();
    }

    public static class ClaimSelection {
        private ClaimCorner pos1;
        private ClaimCorner pos2;

        public ClaimCorner getPos1() {
            return pos1;
        }

        public ClaimCorner getPos2() {
            return pos2;
        }
    }
}
