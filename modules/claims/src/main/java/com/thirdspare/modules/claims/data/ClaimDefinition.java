package com.thirdspare.modules.claims.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClaimDefinition {
    private String id;
    private String displayName;
    private UUID ownerUuid;
    private String ownerName;
    private UUID worldUuid;
    private Set<UUID> trustedUuids;
    private Map<String, String> trustedNames;
    private ClaimBounds bounds;
    private long createdAtEpochSeconds;
    private boolean enabled;

    public ClaimDefinition() {
        this.id = "";
        this.displayName = "";
        this.ownerName = "";
        this.trustedUuids = new HashSet<>();
        this.trustedNames = new HashMap<>();
        this.bounds = new ClaimBounds();
        this.enabled = true;
    }

    public ClaimDefinition(String id, String displayName, UUID ownerUuid, String ownerName,
                           UUID worldUuid, ClaimBounds bounds) {
        this();
        this.id = id != null ? id : "";
        this.displayName = normalizeName(displayName);
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName != null ? ownerName : "";
        this.worldUuid = worldUuid;
        this.bounds = bounds != null ? bounds : new ClaimBounds();
        this.createdAtEpochSeconds = System.currentTimeMillis() / 1000L;
        this.enabled = true;
    }

    public static String claimId(UUID ownerUuid, String displayName) {
        return ownerUuid + ":" + normalizeName(displayName);
    }

    public static String normalizeName(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase().replace(' ', '_');
    }

    public boolean isOwner(UUID uuid) {
        return ownerUuid != null && ownerUuid.equals(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trustedUuids != null && trustedUuids.contains(uuid);
    }

    public boolean canAccess(UUID uuid) {
        return isOwner(uuid) || isTrusted(uuid);
    }

    public void trust(UUID uuid, String name) {
        if (uuid == null || isOwner(uuid)) return;
        trustedUuids.add(uuid);
        trustedNames.put(uuid.toString(), name != null ? name : uuid.toString());
    }

    public boolean untrust(UUID uuid) {
        if (uuid == null) return false;
        trustedNames.remove(uuid.toString());
        return trustedUuids.remove(uuid);
    }

    public String displayMember(UUID uuid) {
        String name = trustedNames.get(uuid.toString());
        return name == null || name.isBlank() ? uuid.toString() : name;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getOwnerName() { return ownerName; }
    public UUID getWorldUuid() { return worldUuid; }
    public Set<UUID> getTrustedUuids() { return trustedUuids; }
    public Map<String, String> getTrustedNames() { return trustedNames; }
    public ClaimBounds getBounds() { return bounds; }
    public long getCreatedAtEpochSeconds() { return createdAtEpochSeconds; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static final BuilderCodec<ClaimDefinition> CODEC = BuilderCodec.builder(ClaimDefinition.class, ClaimDefinition::new)
            .append(new KeyedCodec<>("Id", Codec.STRING),
                    (c, v) -> c.id = v != null ? v : "",
                    c -> c.id).add()
            .append(new KeyedCodec<>("DisplayName", Codec.STRING),
                    (c, v) -> c.displayName = normalizeName(v),
                    c -> c.displayName).add()
            .append(new KeyedCodec<>("OwnerUuid", Codec.UUID_STRING),
                    (c, v) -> c.ownerUuid = v,
                    c -> c.ownerUuid).add()
            .append(new KeyedCodec<>("OwnerName", Codec.STRING),
                    (c, v) -> c.ownerName = v != null ? v : "",
                    c -> c.ownerName).add()
            .append(new KeyedCodec<>("WorldUuid", Codec.UUID_STRING),
                    (c, v) -> c.worldUuid = v,
                    c -> c.worldUuid).add()
            .append(new KeyedCodec<>("TrustedUuids",
                    new SetCodec<>(Codec.UUID_STRING, HashSet::new, false)),
                    (c, v) -> c.trustedUuids = v != null ? new HashSet<>(v) : new HashSet<>(),
                    c -> c.trustedUuids).add()
            .append(new KeyedCodec<>("TrustedNames",
                    new ObjectMapCodec<>(
                            Codec.STRING,
                            HashMap::new,
                            key -> key,
                            str -> str
                    )),
                    (c, v) -> c.trustedNames = v != null ? new HashMap<>(v) : new HashMap<>(),
                    c -> c.trustedNames).add()
            .append(new KeyedCodec<>("Bounds", ClaimBounds.CODEC),
                    (c, v) -> c.bounds = v != null ? v : new ClaimBounds(),
                    c -> c.bounds).add()
            .append(new KeyedCodec<>("CreatedAtEpochSeconds", Codec.LONG),
                    (c, v) -> c.createdAtEpochSeconds = v != null ? v : 0L,
                    c -> c.createdAtEpochSeconds).add()
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (c, v) -> c.enabled = v == null || v,
                    c -> c.enabled).add()
            .build();
}
