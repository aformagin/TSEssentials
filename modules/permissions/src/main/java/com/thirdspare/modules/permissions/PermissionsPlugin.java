package com.thirdspare.modules.permissions;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.api.TSEssentialsApi;
import com.thirdspare.modules.permissions.commands.PermissionsCommand;
import com.thirdspare.modules.permissions.commands.PermissionsUICommand;
import com.thirdspare.modules.permissions.component.PlayerPermissionMembershipComponent;
import com.thirdspare.modules.permissions.data.PermissionsGroupsConfig;
import com.thirdspare.modules.permissions.data.PermissionsUsersConfig;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class PermissionsPlugin extends JavaPlugin {
    private static final String PERMISSIONS_ADMIN_UI = "PermissionsAdmin.ui";

    private final Config<PermissionsGroupsConfig> groupsConfig =
            withConfig("permissions_groups", PermissionsGroupsConfig.CODEC);
    private final Config<PermissionsUsersConfig> usersConfig =
            withConfig("permissions_users", PermissionsUsersConfig.CODEC);

    private PermissionsService service;
    private TSEPermissionsProvider provider;

    public PermissionsPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        TSEssentialsApi.registerPermissionConstants(TSEPermissionsNodes.class,
                "TSEssentials Permissions", "Permissions plugin command");

        PermissionsManager manager = new PermissionsManager(groupsConfig, groupsConfig.get(), usersConfig, usersConfig.get());
        ComponentType<EntityStore, PlayerPermissionMembershipComponent> componentType = getEntityStoreRegistry()
                .registerComponent(
                        PlayerPermissionMembershipComponent.class,
                        "TSEssentials_PlayerPermissionMembership",
                        PlayerPermissionMembershipComponent.CODEC
                );
        PermissionNodeRegistry nodeRegistry = new PermissionNodeRegistry();
        nodeRegistry.registerAll(TSEssentialsApi.permissionNodes());
        service = new PermissionsService(manager, componentType, nodeRegistry);
        provider = new TSEPermissionsProvider(service);

        getCommandRegistry().registerCommand(new PermissionsCommand(service, PERMISSIONS_ADMIN_UI));
        getCommandRegistry().registerCommand(new PermissionsUICommand(service, PERMISSIONS_ADMIN_UI));
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> service.onPlayerReady(event.getPlayer().getPlayerRef()));
        getLogger().at(Level.INFO).log("Registered TSEssentials Permissions plugin.");
    }

    @Override
    protected void start() {
        PermissionsModule.get().addProvider(provider);
    }

    @Override
    protected void shutdown() {
        if (provider != null) {
            PermissionsModule.get().removeProvider(provider);
        }
    }
}
