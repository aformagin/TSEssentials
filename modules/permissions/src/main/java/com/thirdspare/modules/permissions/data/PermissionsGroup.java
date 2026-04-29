package com.thirdspare.modules.permissions.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.HashSet;
import java.util.Set;

public class PermissionsGroup {
    private String name;
    private String displayName;
    private Set<String> permissionNodes;
    private boolean protectedGroup;
    private boolean defaultGroup;

    public PermissionsGroup() {
        this.name = "";
        this.displayName = "";
        this.permissionNodes = new HashSet<>();
        this.protectedGroup = false;
        this.defaultGroup = false;
    }

    public PermissionsGroup(String name, String displayName) {
        this();
        setName(name);
        setDisplayName(displayName);
    }

    public static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeName(name);
    }

    public String getDisplayName() {
        return displayName == null || displayName.isBlank() ? name : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName.trim() : "";
    }

    public Set<String> getPermissionNodes() {
        return permissionNodes;
    }

    public void setPermissionNodes(Set<String> permissionNodes) {
        this.permissionNodes = normalizeNodes(permissionNodes);
    }

    public boolean isProtectedGroup() {
        return protectedGroup;
    }

    public void setProtectedGroup(boolean protectedGroup) {
        this.protectedGroup = protectedGroup;
    }

    public boolean isDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(boolean defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public static Set<String> normalizeNodes(Set<String> values) {
        Set<String> nodes = new HashSet<>();
        if (values != null) {
            for (String value : values) {
                String normalized = value == null ? "" : value.trim().toLowerCase();
                if (!normalized.isBlank()) {
                    nodes.add(normalized);
                }
            }
        }
        return nodes;
    }

    public static final BuilderCodec<PermissionsGroup> CODEC =
            BuilderCodec.builder(PermissionsGroup.class, PermissionsGroup::new)
                    .append(new KeyedCodec<>("Name", Codec.STRING),
                            PermissionsGroup::setName,
                            PermissionsGroup::getName).add()
                    .append(new KeyedCodec<>("DisplayName", Codec.STRING),
                            PermissionsGroup::setDisplayName,
                            PermissionsGroup::getDisplayName).add()
                    .append(new KeyedCodec<>("PermissionNodes",
                            new SetCodec<>(Codec.STRING, HashSet::new, false)),
                            PermissionsGroup::setPermissionNodes,
                            PermissionsGroup::getPermissionNodes).add()
                    .append(new KeyedCodec<>("ProtectedGroup", Codec.BOOLEAN),
                            PermissionsGroup::setProtectedGroup,
                            PermissionsGroup::isProtectedGroup).add()
                    .append(new KeyedCodec<>("DefaultGroup", Codec.BOOLEAN),
                            PermissionsGroup::setDefaultGroup,
                            PermissionsGroup::isDefaultGroup).add()
                    .build();
}
