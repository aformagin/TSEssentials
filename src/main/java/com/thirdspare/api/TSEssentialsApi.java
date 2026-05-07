package com.thirdspare.api;

import com.thirdspare.modules.core.PermissionNodeDescriptor;
import com.thirdspare.permissions.TSEssentialsPermissions;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public final class TSEssentialsApi {
    private static final Map<String, PermissionNodeDescriptor> PERMISSION_NODES =
            new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private TSEssentialsApi() {
    }

    public static void registerCorePermissionConstants() {
        registerPermissionConstants(TSEssentialsPermissions.class, "TSEssentials Core", "Core command permission");
    }

    public static void registerPermissionConstants(Class<?> type, String source, String description) {
        if (type == null) {
            return;
        }
        for (var field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || field.getType() != String.class) {
                continue;
            }
            try {
                String value = (String) field.get(null);
                registerPermissionNode(new PermissionNodeDescriptor(value, source, description));
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static void registerPermissionNodes(Collection<PermissionNodeDescriptor> nodes) {
        if (nodes != null) {
            nodes.forEach(TSEssentialsApi::registerPermissionNode);
        }
    }

    public static void registerPermissionNode(PermissionNodeDescriptor node) {
        if (node != null && node.node() != null && !node.node().isBlank()) {
            synchronized (PERMISSION_NODES) {
                PERMISSION_NODES.put(node.node(), node);
            }
        }
    }

    public static Collection<PermissionNodeDescriptor> permissionNodes() {
        synchronized (PERMISSION_NODES) {
            return new ArrayList<>(PERMISSION_NODES.values());
        }
    }
}
