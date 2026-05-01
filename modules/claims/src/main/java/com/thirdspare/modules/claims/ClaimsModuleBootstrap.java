package com.thirdspare.modules.claims;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.api.TSEModule;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.claims.commands.ClaimBypassCommand;
import com.thirdspare.modules.claims.commands.ClaimCommand;
import com.thirdspare.modules.claims.commands.ClaimTrustCommand;
import com.thirdspare.modules.claims.commands.ClaimUntrustCommand;
import com.thirdspare.modules.claims.data.ClaimsConfig;
import com.thirdspare.modules.claims.events.ClaimsProtectionListener;
import com.thirdspare.modules.claims.events.ClaimsProtectionSystems;
import com.thirdspare.modules.core.PermissionCatalogContributor;
import com.thirdspare.modules.core.PermissionNodeDescriptor;

import java.util.Collection;
import java.util.logging.Level;

public final class ClaimsModuleBootstrap implements TSEModule, PermissionCatalogContributor {
    private static final TSEModuleDescriptor DESCRIPTOR = new TSEModuleDescriptor(
            "tsessentials-claims",
            "TSEssentials Claims",
            "1.0",
            "1.1.0",
            ""
    );

    private ClaimsService claimsService;
    private ClaimBypassService bypassService;
    private ClaimSelectionService selectionService;

    @Override
    public TSEModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void register(TSEModuleContext context) throws Exception {
        Config<ClaimsConfig> claimsConfig = context.registerConfig("claims_data", ClaimsConfig.CODEC);
        ClaimsConfig config = claimsConfig.get();
        if (config == null) {
            config = new ClaimsConfig();
        }

        ClaimsManager claimsManager = new ClaimsManager(claimsConfig, config);
        selectionService = new ClaimSelectionService();
        bypassService = new ClaimBypassService();
        claimsService = new ClaimsService(claimsManager, selectionService, bypassService);

        TSEUiDocument membersUi = context.registerUiDocument(
                "ClaimMembers.ui",
                "Common/UI/Custom/ClaimMembers.ui"
        );
        TSEUiDocument adminUi = context.registerUiDocument(
                "ClaimsAdmin.ui",
                "Common/UI/Custom/ClaimsAdmin.ui"
        );
        TSEUiDocument viewUi = context.registerUiDocument(
                "ClaimView.ui",
                "Common/UI/Custom/ClaimView.ui"
        );

        context.registerCommand(new ClaimCommand(claimsService, membersUi));
        context.registerCommand(new ClaimBypassCommand(claimsService));
        context.registerCommand(new ClaimTrustCommand(claimsService));
        context.registerCommand(new ClaimUntrustCommand(claimsService));

        ClaimsProtectionListener listener = new ClaimsProtectionListener(claimsService);
        context.eventRegistry().registerGlobal(PlayerInteractEvent.class, listener::onPlayerInteract);

        context.registerEntitySystem(new ClaimsProtectionSystems.BreakBlockSystem(claimsService));
        context.registerEntitySystem(new ClaimsProtectionSystems.DamageBlockSystem(claimsService));
        context.registerEntitySystem(new ClaimsProtectionSystems.PlaceBlockSystem(claimsService));
        context.registerEntitySystem(new ClaimsProtectionSystems.UseBlockPreSystem(claimsService));

        context.logger().at(Level.INFO).log(
                "TSEssentials Claims module registered. Loaded "
                        + claimsManager.getConfig().getClaimCount() + " claim(s).");
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
        if (selectionService != null) selectionService.clearAll();
        if (bypassService != null) bypassService.clearAll();
    }

    @Override
    public Collection<PermissionNodeDescriptor> permissionNodes() {
        return ClaimsPermissionNodes.permissionNodes();
    }
}
