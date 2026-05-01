package com.thirdspare.data.claims;

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

public class Claim {
    private String id;
    private String name;
    private UUID ownerUuid;
    private String ownerName;
    private Set<UUID> trustedUuids;
    private Map<String, String> trustedNames;
    private CuboidRegion region;

    public Claim() {
        this.id = "";
        this.name = "";
        this.ownerName = "";
        this.trustedUuids = new HashSet<>();
        this.trustedNames = new HashMap<>();
        this.region = new CuboidRegion();
    }

    public Claim(String id, String name, UUID ownerUuid, String ownerName, CuboidRegion region) {
        this();
        this.id = id != null ? id : "";
        this.name = normalizeName(name);
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName != null ? ownerName : "";
        this.region = region != null ? region : new CuboidRegion();
    }

    public static String claimId(UUID ownerUuid, String name) {
        return ownerUuid + ":" + normalizeName(name);
    }

    public static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase();
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
        if (uuid == null || isOwner(uuid)) {
            return;
        }
        trustedUuids.add(uuid);
        trustedNames.put(uuid.toString(), name != null ? name : uuid.toString());
    }

    public boolean untrust(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        trustedNames.remove(uuid.toString());
        return trustedUuids.remove(uuid);
    }

    public String displayMember(UUID uuid) {
        String name = trustedNames.get(uuid.toString());
        return name == null || name.isBlank() ? uuid.toString() : name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Set<UUID> getTrustedUuids() {
        return trustedUuids;
    }

    public CuboidRegion getRegion() {
        return region;
    }

    public static final BuilderCodec<Claim> CODEC = BuilderCodec.builder(Claim.class, Claim::new)
            .append(new KeyedCodec<>("Id", Codec.STRING),
                    (claim, value) -> claim.id = value != null ? value : "",
                    claim -> claim.id).add()
            .append(new KeyedCodec<>("Name", Codec.STRING),
                    (claim, value) -> claim.name = normalizeName(value),
                    claim -> claim.name).add()
            .append(new KeyedCodec<>("OwnerUuid", Codec.UUID_STRING),
                    (claim, value) -> claim.ownerUuid = value,
                    claim -> claim.ownerUuid).add()
            .append(new KeyedCodec<>("OwnerName", Codec.STRING),
                    (claim, value) -> claim.ownerName = value != null ? value : "",
                    claim -> claim.ownerName).add()
            .append(new KeyedCodec<>("TrustedUuids",
                    new SetCodec<>(Codec.UUID_STRING, HashSet::new, false)),
                    (claim, value) -> claim.trustedUuids = value != null ? new HashSet<>(value) : new HashSet<>(),
                    claim -> claim.trustedUuids).add()
            .append(new KeyedCodec<>("TrustedNames",
                    new ObjectMapCodec<>(
                            Codec.STRING,
                            HashMap::new,
                            key -> key,
                            str -> str
                    )),
                    (claim, value) -> claim.trustedNames = value != null ? new HashMap<>(value) : new HashMap<>(),
                    claim -> claim.trustedNames).add()
            .append(new KeyedCodec<>("Region", CuboidRegion.CODEC),
                    (claim, value) -> claim.region = value != null ? value : new CuboidRegion(),
                    claim -> claim.region).add()
            .build();
}
