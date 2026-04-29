package com.thirdspare.modules.permissions.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionsUsersConfig {
    private int schemaVersion;
    private Map<String, PermissionsUserRecord> users;

    public PermissionsUsersConfig() {
        this.schemaVersion = 1;
        this.users = new HashMap<>();
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = Math.max(1, schemaVersion);
    }

    public Map<String, PermissionsUserRecord> getUsers() {
        return users;
    }

    public Collection<PermissionsUserRecord> getAllUsers() {
        return users.values();
    }

    public PermissionsUserRecord getUser(UUID uuid) {
        return uuid == null ? null : users.get(uuid.toString());
    }

    public PermissionsUserRecord ensureUser(UUID uuid, String username) {
        PermissionsUserRecord record = getUser(uuid);
        if (record == null) {
            record = new PermissionsUserRecord(uuid, username);
            users.put(uuid.toString(), record);
        } else if (username != null && !username.isBlank()) {
            record.setLastKnownUsername(username);
        }
        return record;
    }

    public PermissionsUserRecord findByUsername(String username) {
        String normalized = normalizeUsername(username);
        if (normalized.isBlank()) {
            return null;
        }
        for (PermissionsUserRecord record : users.values()) {
            if (normalizeUsername(record.getLastKnownUsername()).equals(normalized)) {
                return record;
            }
        }
        return null;
    }

    public PermissionsUserRecord findByNameOrUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            PermissionsUserRecord record = getUser(UUID.fromString(value.trim()));
            if (record != null) {
                return record;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return findByUsername(value);
    }

    public void normalizeKeys() {
        Map<String, PermissionsUserRecord> normalized = new HashMap<>();
        if (users != null) {
            for (PermissionsUserRecord record : users.values()) {
                UUID uuid = record != null ? record.getUuid() : null;
                if (uuid != null) {
                    normalized.put(uuid.toString(), record);
                }
            }
        }
        users = normalized;
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    public static final BuilderCodec<PermissionsUsersConfig> CODEC =
            BuilderCodec.builder(PermissionsUsersConfig.class, PermissionsUsersConfig::new)
                    .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER),
                            PermissionsUsersConfig::setSchemaVersion,
                            PermissionsUsersConfig::getSchemaVersion).add()
                    .append(new KeyedCodec<>("Users",
                            new ObjectMapCodec<>(
                                    PermissionsUserRecord.CODEC,
                                    HashMap::new,
                                    key -> key,
                                    str -> str
                            )),
                            (config, value) -> {
                                config.users = value != null ? new HashMap<>(value) : new HashMap<>();
                                config.normalizeKeys();
                            },
                            PermissionsUsersConfig::getUsers).add()
                    .build();
}
