package com.thirdspare.modules.permissions;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.api.TSEModule;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.permissions.commands.PermissionsCommand;
import com.thirdspare.modules.permissions.commands.PermissionsUICommand;
import com.thirdspare.modules.permissions.component.PlayerPermissionMembershipComponent;
import com.thirdspare.modules.permissions.data.PermissionsGroupsConfig;
import com.thirdspare.modules.permissions.data.PermissionsUsersConfig;

import java.util.logging.Level;

public class PermissionsModuleBootstrap implements TSEModule {
    private static final TSEModuleDescriptor DESCRIPTOR = new TSEModuleDescriptor(
            "permissions",
            "TSEssentials Permissions",
            "1.1.0-PERMISSIONS-SNAPSHOT",
            "1.1.0",
            ""
    );

    private PermissionsService service;
    private TSEPermissionsProvider provider;

    @Override
    public TSEModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void register(TSEModuleContext context) {
        Config<PermissionsGroupsConfig> groupsConfig = context.registerConfig("permissions_groups", PermissionsGroupsConfig.CODEC);
        Config<PermissionsUsersConfig> usersConfig = context.registerConfig("permissions_users", PermissionsUsersConfig.CODEC);
        PermissionsManager manager = new PermissionsManager(groupsConfig, groupsConfig.get(), usersConfig, usersConfig.get());
        ComponentType<EntityStore, PlayerPermissionMembershipComponent> componentType = context.registerComponent(
                PlayerPermissionMembershipComponent.class,
                "TSEssentials_PlayerPermissionMembership",
                PlayerPermissionMembershipComponent.CODEC
        );
        PermissionNodeRegistry nodeRegistry = new PermissionNodeRegistry();
        nodeRegistry.registerAll(context.core().getModuleLoader().permissionNodes());
        service = new PermissionsService(manager, componentType, nodeRegistry);
        provider = new TSEPermissionsProvider(service);
        TSEUiDocument permissionsAdminUi = context.registerUiDocument(
                "PermissionsAdmin.ui",
                "Common/UI/Custom/PermissionsAdmin.ui"
        );
        context.registerCommand(new PermissionsCommand(service, permissionsAdminUi));
        context.registerCommand(new PermissionsUICommand(service, permissionsAdminUi));
        context.logger().at(Level.INFO).log("Registered optional TSE permissions module.");
    }

    @Override
    public void enable() {
        PermissionsModule.get().addProvider(provider);
    }

    @Override
    public void disable() {
        if (provider != null) {
            PermissionsModule.get().removeProvider(provider);
        }
    }

    @Override
    public void onPlayerReady(PlayerRef player) {
        if (service != null) {
            service.onPlayerReady(player);
        }
    }
}
