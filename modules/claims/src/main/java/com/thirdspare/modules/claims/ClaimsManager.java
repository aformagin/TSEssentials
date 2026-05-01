package com.thirdspare.modules.claims;

import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.claims.data.ClaimBounds;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.thirdspare.modules.claims.data.ClaimLookupKey;
import com.thirdspare.modules.claims.data.ClaimsConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ClaimsManager {
    private final Config<ClaimsConfig> config;
    private ClaimsConfig claimsConfig;

    private final Map<ClaimLookupKey, Set<String>> spatialIndex = new HashMap<>();

    public ClaimsManager(Config<ClaimsConfig> config, ClaimsConfig claimsConfig) {
        this.config = config;
        this.claimsConfig = claimsConfig;
        rebuildIndex();
    }

    public ClaimsConfig getConfig() {
        return claimsConfig;
    }

    public Collection<ClaimDefinition> getClaims() {
        return List.copyOf(claimsConfig.getAllClaims());
    }

    public Optional<ClaimDefinition> getClaim(String claimId) {
        return Optional.ofNullable(claimsConfig.getClaim(claimId));
    }

    public Optional<ClaimDefinition> getClaimAt(UUID worldUuid, int x, int y, int z) {
        if (worldUuid == null) return Optional.empty();
        ClaimLookupKey key = ClaimLookupKey.of(worldUuid, x, z);
        Set<String> candidateIds = spatialIndex.getOrDefault(key, Collections.emptySet());
        for (String id : candidateIds) {
            ClaimDefinition claim = claimsConfig.getClaim(id);
            if (claim != null && claim.isEnabled()
                    && worldUuid.equals(claim.getWorldUuid())
                    && claim.getBounds().contains(x, y, z)) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    public String createClaim(ClaimDefinition claim) {
        if (claim == null || claim.getId() == null || claim.getId().isBlank()) {
            return "Invalid claim definition.";
        }
        if (claimsConfig.getClaim(claim.getId()) != null) {
            return "A claim with this name already exists.";
        }
        String overlapError = validateNoOverlap(claim.getWorldUuid(), claim.getBounds(), null);
        if (overlapError != null) return overlapError;

        claimsConfig.putClaim(claim);
        indexClaim(claim);
        save();
        return null;
    }

    public String deleteClaim(String claimId) {
        ClaimDefinition claim = claimsConfig.getClaim(claimId);
        if (claim == null) return "Unknown claim.";
        claimsConfig.removeClaim(claimId);
        unindexClaim(claim);
        save();
        return null;
    }

    public String trust(String claimId, UUID trustedUuid, String trustedName) {
        ClaimDefinition claim = claimsConfig.getClaim(claimId);
        if (claim == null) return "Unknown claim.";
        if (claim.isOwner(trustedUuid)) return "The owner already has access.";
        claim.trust(trustedUuid, trustedName);
        save();
        return null;
    }

    public String untrust(String claimId, UUID trustedUuid) {
        ClaimDefinition claim = claimsConfig.getClaim(claimId);
        if (claim == null) return "Unknown claim.";
        if (!claim.untrust(trustedUuid)) return "That player is not trusted on this claim.";
        save();
        return null;
    }

    public CompletableFuture<Void> save() {
        return config.save();
    }

    public void rebuildIndex() {
        spatialIndex.clear();
        for (ClaimDefinition claim : claimsConfig.getAllClaims()) {
            if (claim.isEnabled()) {
                indexClaim(claim);
            }
        }
    }

    private void indexClaim(ClaimDefinition claim) {
        if (claim.getWorldUuid() == null || claim.getBounds() == null) return;
        ClaimBounds b = claim.getBounds();
        int chunkMinX = b.getMinX() >> 4;
        int chunkMaxX = b.getMaxX() >> 4;
        int chunkMinZ = b.getMinZ() >> 4;
        int chunkMaxZ = b.getMaxZ() >> 4;
        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                ClaimLookupKey key = new ClaimLookupKey(claim.getWorldUuid(), cx, cz);
                spatialIndex.computeIfAbsent(key, k -> new HashSet<>()).add(claim.getId());
            }
        }
    }

    private void unindexClaim(ClaimDefinition claim) {
        if (claim.getWorldUuid() == null || claim.getBounds() == null) return;
        ClaimBounds b = claim.getBounds();
        int chunkMinX = b.getMinX() >> 4;
        int chunkMaxX = b.getMaxX() >> 4;
        int chunkMinZ = b.getMinZ() >> 4;
        int chunkMaxZ = b.getMaxZ() >> 4;
        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                ClaimLookupKey key = new ClaimLookupKey(claim.getWorldUuid(), cx, cz);
                Set<String> ids = spatialIndex.get(key);
                if (ids != null) {
                    ids.remove(claim.getId());
                    if (ids.isEmpty()) spatialIndex.remove(key);
                }
            }
        }
    }

    private String validateNoOverlap(UUID worldUuid, ClaimBounds bounds, String ignoredId) {
        if (worldUuid == null || bounds == null) return null;
        ClaimLookupKey key = ClaimLookupKey.of(worldUuid, bounds.getMinX(), bounds.getMinZ());
        Set<String> checked = new HashSet<>();
        // Check all chunks covered by the new bounds
        int chunkMinX = bounds.getMinX() >> 4;
        int chunkMaxX = bounds.getMaxX() >> 4;
        int chunkMinZ = bounds.getMinZ() >> 4;
        int chunkMaxZ = bounds.getMaxZ() >> 4;
        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                Set<String> candidates = spatialIndex.getOrDefault(
                        new ClaimLookupKey(worldUuid, cx, cz), Collections.emptySet());
                for (String id : candidates) {
                    if (!checked.add(id)) continue;
                    if (id.equals(ignoredId)) continue;
                    ClaimDefinition existing = claimsConfig.getClaim(id);
                    if (existing != null && existing.isEnabled()
                            && worldUuid.equals(existing.getWorldUuid())
                            && existing.getBounds().overlaps(bounds)) {
                        return "That region overlaps claim '" + existing.getDisplayName() + "'.";
                    }
                }
            }
        }
        return null;
    }
}
