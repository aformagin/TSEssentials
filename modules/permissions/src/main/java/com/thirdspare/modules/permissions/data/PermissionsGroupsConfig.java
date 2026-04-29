package com.thirdspare.modules.permissions.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PermissionsGroupsConfig {
    private int schemaVersion;
    private Map<String, PermissionsGroup> groups;

    public PermissionsGroupsConfig() {
        this.schemaVersion = 1;
        this.groups = new HashMap<>();
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = Math.max(1, schemaVersion);
    }

    public Map<String, PermissionsGroup> getGroups() {
        return groups;
    }

    public Collection<PermissionsGroup> getAllGroups() {
        return groups.values();
    }

    public PermissionsGroup getGroup(String name) {
        return groups.get(PermissionsGroup.normalizeName(name));
    }

    public boolean hasGroup(String name) {
        return groups.containsKey(PermissionsGroup.normalizeName(name));
    }

    public void setGroup(PermissionsGroup group) {
        if (group != null && !group.getName().isBlank()) {
            groups.put(PermissionsGroup.normalizeName(group.getName()), group);
        }
    }

    public boolean removeGroup(String name) {
        return groups.remove(PermissionsGroup.normalizeName(name)) != null;
    }

    public void normalizeKeys() {
        Map<String, PermissionsGroup> normalized = new HashMap<>();
        if (groups != null) {
            for (PermissionsGroup group : groups.values()) {
                if (group != null && !group.getName().isBlank()) {
                    normalized.put(PermissionsGroup.normalizeName(group.getName()), group);
                }
            }
        }
        groups = normalized;
    }

    public static final BuilderCodec<PermissionsGroupsConfig> CODEC =
            BuilderCodec.builder(PermissionsGroupsConfig.class, PermissionsGroupsConfig::new)
                    .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER),
                            PermissionsGroupsConfig::setSchemaVersion,
                            PermissionsGroupsConfig::getSchemaVersion).add()
                    .append(new KeyedCodec<>("Groups",
                            new ObjectMapCodec<>(
                                    PermissionsGroup.CODEC,
                                    HashMap::new,
                                    key -> key,
                                    str -> str
                            )),
                            (config, value) -> {
                                config.groups = value != null ? new HashMap<>(value) : new HashMap<>();
                                config.normalizeKeys();
                            },
                            PermissionsGroupsConfig::getGroups).add()
                    .build();
}
