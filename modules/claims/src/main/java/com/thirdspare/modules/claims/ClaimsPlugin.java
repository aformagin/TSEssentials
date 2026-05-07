package com.thirdspare.modules.claims;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.api.TSEssentialsApi;
import com.thirdspare.modules.claims.commands.ClaimBypassCommand;
import com.thirdspare.modules.claims.commands.ClaimCommand;
import com.thirdspare.modules.claims.commands.ClaimTrustCommand;
import com.thirdspare.modules.claims.commands.ClaimUntrustCommand;
import com.thirdspare.modules.claims.data.ClaimsConfig;
import com.thirdspare.modules.claims.events.ClaimsProtectionListener;
import com.thirdspare.modules.claims.events.ClaimsProtectionSystems;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public final class ClaimsPlugin extends JavaPlugin {
    private static final String CLAIM_MEMBERS_UI = "ClaimMembers.ui";

    private final Config<ClaimsConfig> claimsConfig = withConfig("claims_data", ClaimsConfig.CODEC);
    private ClaimBypassService bypassService;
    private ClaimSelectionService selectionService;

    public ClaimsPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        TSEssentialsApi.registerPermissionNodes(ClaimsPermissionNodes.permissionNodes());

        ClaimsConfig config = claimsConfig.get();
        if (config == null) {
            config = new ClaimsConfig();
        }

        ClaimsManager claimsManager = new ClaimsManager(claimsConfig, config);
        selectionService = new ClaimSelectionService();
        bypassService = new ClaimBypassService();
        ClaimsService claimsService = new ClaimsService(claimsManager, selectionService, bypassService);

        getCommandRegistry().registerCommand(new ClaimCommand(claimsService, CLAIM_MEMBERS_UI));
        getCommandRegistry().registerCommand(new ClaimBypassCommand(claimsService));
        getCommandRegistry().registerCommand(new ClaimTrustCommand(claimsService));
        getCommandRegistry().registerCommand(new ClaimUntrustCommand(claimsService));

        ClaimsProtectionListener listener = new ClaimsProtectionListener(claimsService);
        getEventRegistry().registerGlobal(PlayerInteractEvent.class, listener::onPlayerInteract);

        getEntityStoreRegistry().registerSystem(new ClaimsProtectionSystems.BreakBlockSystem(claimsService));
        getEntityStoreRegistry().registerSystem(new ClaimsProtectionSystems.DamageBlockSystem(claimsService));
        getEntityStoreRegistry().registerSystem(new ClaimsProtectionSystems.PlaceBlockSystem(claimsService));
        getEntityStoreRegistry().registerSystem(new ClaimsProtectionSystems.UseBlockPreSystem(claimsService));

        getLogger().at(Level.INFO).log(
                "TSEssentials Claims plugin registered. Loaded "
                        + claimsManager.getConfig().getClaimCount() + " claim(s).");
    }

    @Override
    protected void shutdown() {
        if (selectionService != null) selectionService.clearAll();
        if (bypassService != null) bypassService.clearAll();
    }
}
