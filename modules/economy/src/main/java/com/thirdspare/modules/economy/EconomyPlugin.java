package com.thirdspare.modules.economy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.economy.commands.BalanceCommand;
import com.thirdspare.modules.economy.commands.EcoCommand;
import com.thirdspare.modules.economy.commands.EconAdminUICommand;
import com.thirdspare.modules.economy.commands.PayCommand;
import com.thirdspare.modules.economy.commands.WalletCommand;
import com.thirdspare.modules.economy.component.PlayerEconomyComponent;
import com.thirdspare.modules.economy.data.EconomyAccountsConfig;
import com.thirdspare.modules.economy.data.EconomyConfig;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class EconomyPlugin extends JavaPlugin {
    private static final String ECONOMY_UI = "Economy.ui";
    private static final String ECONOMY_ADMIN_UI = "EconomyAdmin.ui";

    private final Config<EconomyConfig> economyConfig = withConfig("economy_config", EconomyConfig.CODEC);
    private final Config<EconomyAccountsConfig> accountsConfig = withConfig("economy_accounts", EconomyAccountsConfig.CODEC);
    private EconomyService service;

    public EconomyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ComponentType<EntityStore, PlayerEconomyComponent> componentType = getEntityStoreRegistry()
                .registerComponent(
                        PlayerEconomyComponent.class,
                        "TSEssentials_PlayerEconomy",
                        PlayerEconomyComponent.CODEC
                );
        EconomyManager manager = new EconomyManager(economyConfig, economyConfig.get(), accountsConfig, accountsConfig.get());
        service = new EconomyService(manager, componentType);

        getCommandRegistry().registerCommand(new BalanceCommand(service));
        getCommandRegistry().registerCommand(new PayCommand(service));
        getCommandRegistry().registerCommand(new WalletCommand(service, ECONOMY_UI));
        getCommandRegistry().registerCommand(new EcoCommand(service));
        getCommandRegistry().registerCommand(new EconAdminUICommand(service, ECONOMY_ADMIN_UI));
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> service.loadEconomy(event.getPlayer().getPlayerRef()));

        getLogger().at(Level.INFO).log("Loaded " + accountsConfig.get().getAccounts().size() + " economy accounts.");
    }
}
