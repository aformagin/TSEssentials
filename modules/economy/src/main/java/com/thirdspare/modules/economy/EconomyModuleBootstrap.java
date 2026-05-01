package com.thirdspare.modules.economy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.api.TSEModule;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.economy.commands.BalanceCommand;
import com.thirdspare.modules.economy.commands.EcoCommand;
import com.thirdspare.modules.economy.commands.EconAdminUICommand;
import com.thirdspare.modules.economy.commands.PayCommand;
import com.thirdspare.modules.economy.commands.WalletCommand;
import com.thirdspare.modules.economy.component.PlayerEconomyComponent;
import com.thirdspare.modules.economy.data.EconomyAccountsConfig;
import com.thirdspare.modules.economy.data.EconomyConfig;

import java.util.logging.Level;

public class EconomyModuleBootstrap implements TSEModule {
    private static final TSEModuleDescriptor DESCRIPTOR = new TSEModuleDescriptor(
            "economy",
            "TSEssentials Economy",
            "1.1.0-ECON-SNAPSHOT",
            "1.1.0",
            ""
    );

    private EconomyService service;

    @Override
    public TSEModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void register(TSEModuleContext context) {
        Config<EconomyConfig> economyConfig = context.registerConfig("economy_config", EconomyConfig.CODEC);
        Config<EconomyAccountsConfig> accountsConfig = context.registerConfig("economy_accounts", EconomyAccountsConfig.CODEC);
        ComponentType<EntityStore, PlayerEconomyComponent> componentType = context.registerComponent(
                PlayerEconomyComponent.class,
                "TSEssentials_PlayerEconomy",
                PlayerEconomyComponent.CODEC
        );
        EconomyManager manager = new EconomyManager(economyConfig, economyConfig.get(), accountsConfig, accountsConfig.get());
        service = new EconomyService(manager, componentType);
        TSEUiDocument economyUi = context.registerUiDocument(
                "Economy.ui",
                "Common/UI/Custom/Economy.ui"
        );
        TSEUiDocument economyAdminUi = context.registerUiDocument(
                "EconomyAdmin.ui",
                "Common/UI/Custom/EconomyAdmin.ui"
        );
        context.registerCommand(new BalanceCommand(service));
        context.registerCommand(new PayCommand(service));
        context.registerCommand(new WalletCommand(service, economyUi));
        context.registerCommand(new EcoCommand(service));
        context.registerCommand(new EconAdminUICommand(service, economyAdminUi));
        context.logger().at(Level.INFO).log("Loaded " + accountsConfig.get().getAccounts().size() + " economy accounts.");
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    @Override
    public void onPlayerReady(PlayerRef player) {
        if (service != null) {
            service.loadEconomy(player);
        }
    }
}
