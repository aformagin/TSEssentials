package com.thirdspare.modules.permissions;

import com.thirdspare.modules.core.PermissionCatalogContributor;
import com.thirdspare.modules.core.PermissionNodeDescriptor;
import com.thirdspare.permissions.TSEssentialsPermissions;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PermissionNodeRegistry {
    private final Map<String, PermissionNodeDescriptor> nodes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public PermissionNodeRegistry() {
        registerCoreConstants();
        registerConstants(TSEPermissionsNodes.class, "TSEssentials Permissions", "Permissions module command");
        seedHytaleNodes();
    }

    public void register(PermissionNodeDescriptor descriptor) {
        if (descriptor != null && !descriptor.node().isBlank()) {
            nodes.put(descriptor.node(), descriptor);
        }
    }

    public void registerAll(Collection<PermissionNodeDescriptor> descriptors) {
        if (descriptors != null) {
            descriptors.forEach(this::register);
        }
    }

    public void registerContributor(PermissionCatalogContributor contributor) {
        if (contributor != null) {
            registerAll(contributor.permissionNodes());
        }
    }

    public List<PermissionNodeDescriptor> listNodes() {
        return new ArrayList<>(nodes.values());
    }

    private void registerCoreConstants() {
        registerConstants(TSEssentialsPermissions.class, "TSEssentials Core", "Core command permission");
    }

    private void registerConstants(Class<?> type, String source, String description) {
        for (var field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || field.getType() != String.class) {
                continue;
            }
            try {
                String value = (String) field.get(null);
                register(new PermissionNodeDescriptor(value, source, description));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private void seedHytaleNodes() {
        List.of(
                "hytale.command.op.add",
                "hytale.command.op.remove",
                "hytale.command.gamemode",
                "hytale.command.teleport",
                "hytale.command.time",
                "hytale.command.weather",
                "hytale.editor.*",
                "hytale.camera.*"
        ).forEach(node -> register(new PermissionNodeDescriptor(node, "Hytale Seed", "Known Hytale permission")));
    }
}
