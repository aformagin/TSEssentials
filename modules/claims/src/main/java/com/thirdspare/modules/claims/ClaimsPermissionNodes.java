package com.thirdspare.modules.claims;

import com.thirdspare.modules.core.PermissionNodeDescriptor;

import java.util.Collection;
import java.util.List;

public final class ClaimsPermissionNodes {
    public static final String COMMAND = "tsessentials.claim.command";
    public static final String CREATE = "tsessentials.claim.create";
    public static final String DELETE = "tsessentials.claim.delete";
    public static final String TRUST = "tsessentials.claim.trust";
    public static final String INFO = "tsessentials.claim.info";
    public static final String BYPASS = "tsessentials.claim.bypass";
    public static final String ADMIN = "tsessentials.claim.admin";
    public static final String ADMIN_DELETE = "tsessentials.claim.admin.delete";
    public static final String ADMIN_TRUST = "tsessentials.claim.admin.trust";
    public static final String ADMIN_RESIZE = "tsessentials.claim.admin.resize";

    private static final String SOURCE = "tsessentials-claims";

    private ClaimsPermissionNodes() {
    }

    public static Collection<PermissionNodeDescriptor> permissionNodes() {
        return List.of(
                new PermissionNodeDescriptor(COMMAND, SOURCE, "Access the /claim command"),
                new PermissionNodeDescriptor(CREATE, SOURCE, "Create land claims"),
                new PermissionNodeDescriptor(DELETE, SOURCE, "Delete own land claims"),
                new PermissionNodeDescriptor(TRUST, SOURCE, "Trust players on own claims"),
                new PermissionNodeDescriptor(INFO, SOURCE, "View claim information"),
                new PermissionNodeDescriptor(BYPASS, SOURCE, "Toggle claim bypass mode"),
                new PermissionNodeDescriptor(ADMIN, SOURCE, "Admin-level claim access"),
                new PermissionNodeDescriptor(ADMIN_DELETE, SOURCE, "Admin delete any claim"),
                new PermissionNodeDescriptor(ADMIN_TRUST, SOURCE, "Admin trust/untrust on any claim"),
                new PermissionNodeDescriptor(ADMIN_RESIZE, SOURCE, "Admin resize any claim")
        );
    }
}
