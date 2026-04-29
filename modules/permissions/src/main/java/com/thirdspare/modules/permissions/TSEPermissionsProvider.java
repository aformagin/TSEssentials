package com.thirdspare.modules.permissions;

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;

import java.util.Set;
import java.util.UUID;

public class TSEPermissionsProvider implements PermissionProvider {
    private final PermissionsService service;

    public TSEPermissionsProvider(PermissionsService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "TSEssentials-Permissions";
    }

    @Override
    public void addUserPermissions(UUID uuid, Set<String> permissions) {
        // Direct user permissions are intentionally out of scope for v1.
    }

    @Override
    public void removeUserPermissions(UUID uuid, Set<String> permissions) {
        // Direct user permissions are intentionally out of scope for v1.
    }

    @Override
    public Set<String> getUserPermissions(UUID uuid) {
        return Set.of();
    }

    @Override
    public void addGroupPermissions(String group, Set<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                service.addNode(group, permission);
            }
        }
    }

    @Override
    public void removeGroupPermissions(String group, Set<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                service.removeNode(group, permission);
            }
        }
    }

    @Override
    public Set<String> getGroupPermissions(String group) {
        return service.getGroup(group)
                .map(value -> Set.copyOf(value.getPermissionNodes()))
                .orElse(Set.of());
    }

    @Override
    public void addUserToGroup(UUID uuid, String group) {
        if (uuid != null) {
            service.getManager().addUserToGroup(uuid, "", group);
        }
    }

    @Override
    public void removeUserFromGroup(UUID uuid, String group) {
        if (uuid != null) {
            service.getManager().removeUserFromGroup(uuid, group);
        }
    }

    @Override
    public Set<String> getGroupsForUser(UUID uuid) {
        return uuid == null ? Set.of() : service.resolveGroups(uuid);
    }
}
