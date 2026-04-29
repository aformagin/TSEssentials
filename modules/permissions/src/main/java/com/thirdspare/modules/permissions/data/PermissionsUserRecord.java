package com.thirdspare.modules.permissions.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PermissionsUserRecord {
    private String uuid;
    private String lastKnownUsername;
    private Set<String> groups;

    public PermissionsUserRecord() {
        this.uuid = "";
        this.lastKnownUsername = "";
        this.groups = new HashSet<>();
    }

    public PermissionsUserRecord(UUID uuid, String lastKnownUsername) {
        this();
        setUuid(uuid);
        setLastKnownUsername(lastKnownUsername);
    }

    public UUID getUuid() {
        return uuid == null || uuid.isBlank() ? null : UUID.fromString(uuid);
    }

    public String getUuidString() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid != null ? uuid.toString() : "";
    }

    public void setUuidString(String uuid) {
        this.uuid = uuid != null ? uuid : "";
    }

    public String getLastKnownUsername() {
        return lastKnownUsername;
    }

    public void setLastKnownUsername(String lastKnownUsername) {
        this.lastKnownUsername = lastKnownUsername != null ? lastKnownUsername.trim() : "";
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = normalizeGroups(groups);
    }

    public static Set<String> normalizeGroups(Set<String> values) {
        Set<String> normalized = new HashSet<>();
        if (values != null) {
            for (String value : values) {
                String group = PermissionsGroup.normalizeName(value);
                if (!group.isBlank()) {
                    normalized.add(group);
                }
            }
        }
        return normalized;
    }

    public static final BuilderCodec<PermissionsUserRecord> CODEC =
            BuilderCodec.builder(PermissionsUserRecord.class, PermissionsUserRecord::new)
                    .append(new KeyedCodec<>("Uuid", Codec.STRING),
                            PermissionsUserRecord::setUuidString,
                            PermissionsUserRecord::getUuidString).add()
                    .append(new KeyedCodec<>("LastKnownUsername", Codec.STRING),
                            PermissionsUserRecord::setLastKnownUsername,
                            PermissionsUserRecord::getLastKnownUsername).add()
                    .append(new KeyedCodec<>("Groups",
                            new SetCodec<>(Codec.STRING, HashSet::new, false)),
                            PermissionsUserRecord::setGroups,
                            PermissionsUserRecord::getGroups).add()
                    .build();
}
