package com.thirdspare;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.chat.ChannelManager;
import com.thirdspare.chat.ChatService;
import com.thirdspare.chat.PlayerChatSettingsComponent;
import com.thirdspare.commands.*;
import com.thirdspare.commands.chat.*;
import com.thirdspare.commands.economy.BalanceCommand;
import com.thirdspare.commands.economy.EcoCommand;
import com.thirdspare.commands.economy.EconAdminUICommand;
import com.thirdspare.commands.economy.WalletCommand;
import com.thirdspare.commands.economy.PayCommand;
import com.thirdspare.data.chat.ChatChannelConfig;
import com.thirdspare.data.economy.EconomyAccountsConfig;
import com.thirdspare.data.economy.EconomyConfig;
import com.thirdspare.data.PlayerDataConfig;
import com.thirdspare.data.SpawnConfig;
import com.thirdspare.data.WarpConfig;
import com.thirdspare.economy.EconomyManager;
import com.thirdspare.economy.EconomyService;
import com.thirdspare.economy.PlayerEconomyComponent;
import com.thirdspare.events.ExampleEvent;
import com.thirdspare.events.chat.ChatListener;
import com.thirdspare.homes.HomeService;
import com.thirdspare.homes.PlayerHomesComponent;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;
import com.thirdspare.modules.core.ModuleLoader;
import com.thirdspare.modules.core.ModulePaths;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.tpa.TeleportRequestManager;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class TSEssentials extends JavaPlugin {
    // TODO: When updating to Hytale 2026.04+, replace deprecated Vector3d/Vector3f API usage
    // with the newer vector/transform API expected by that server version.

    /* Player data configuration with codec-based JSON persistence */
    private final Config<PlayerDataConfig> playerDataConfig = withConfig("player_data", PlayerDataConfig.CODEC);

    /* Warp data configuration with codec-based JSON persistence */
    private final Config<WarpConfig> warpDataConfig = withConfig("warp_data", WarpConfig.CODEC);

    /* Spawn data configuration with codec-based JSON persistence */
    private final Config<SpawnConfig> spawnDataConfig = withConfig("spawn_data", SpawnConfig.CODEC);

    /* Chat channel configuration with codec-based JSON persistence */
    private final Config<ChatChannelConfig> chatChannelConfig = withConfig("chat_channels", ChatChannelConfig.CODEC);

    /* Economy configuration and known account ledger */
    private final Config<EconomyConfig> economyConfig = withConfig("economy_config", EconomyConfig.CODEC);
    private final Config<EconomyAccountsConfig> economyAccountsConfig = withConfig("economy_accounts", EconomyAccountsConfig.CODEC);

    private PlayerDataConfig playerData;
    private WarpConfig warpData;
    private SpawnConfig spawnData;
    private ChatChannelConfig chatChannelData;
    private EconomyConfig economyData;
    private EconomyAccountsConfig economyAccountsData;
    private ComponentType<EntityStore, PlayerChatSettingsComponent> playerChatSettingsComponentType;
    private ComponentType<EntityStore, PlayerHomesComponent> playerHomesComponentType;
    private ComponentType<EntityStore, PlayerEconomyComponent> playerEconomyComponentType;
    private ChannelManager channelManager;
    private ChatService chatService;
    private HomeService homeService;
    private EconomyManager economyManager;
    private EconomyService economyService;
    private TeleportRequestManager teleportRequestManager;
    private ModuleLoader moduleLoader;

    public TSEssentials(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        playerChatSettingsComponentType = this.getEntityStoreRegistry()
                .registerComponent(
                        PlayerChatSettingsComponent.class,
                        "TSEssentials_PlayerChatSettings",
                        PlayerChatSettingsComponent.CODEC
                );
        playerHomesComponentType = this.getEntityStoreRegistry()
                .registerComponent(
                        PlayerHomesComponent.class,
                        "TSEssentials_PlayerHomes",
                        PlayerHomesComponent.CODEC
                );
        playerEconomyComponentType = this.getEntityStoreRegistry()
                .registerComponent(
                        PlayerEconomyComponent.class,
                        "TSEssentials_PlayerEconomy",
                        PlayerEconomyComponent.CODEC
                );

        /* Set up data directory - automatically handled by withConfig */
        /* This will create a data directory, player_data.json and warp_data.json files */
        playerData = playerDataConfig.get();
        this.getLogger().at(Level.INFO).log("Loaded player homes for " +
                (playerData.getPlayerHomes() != null ? playerData.getPlayerHomes().size() : 0) + " players");
        homeService = new HomeService(playerData, playerHomesComponentType);

        warpData = warpDataConfig.get();
        this.getLogger().at(Level.INFO).log("Loaded " + warpData.getWarpCount() + " server warps");

        spawnData = spawnDataConfig.get();
        this.getLogger().at(Level.INFO).log("Server spawn " + (spawnData.hasSpawn() ? "loaded" : "not set"));

        chatChannelData = chatChannelConfig.get();
        channelManager = new ChannelManager(chatChannelConfig, chatChannelData);
        chatService = new ChatService(channelManager, playerChatSettingsComponentType);
        this.getLogger().at(Level.INFO).log("Loaded " + channelManager.getChannels().size() + " chat channels");

        economyData = economyConfig.get();
        economyAccountsData = economyAccountsConfig.get();
        economyManager = new EconomyManager(economyConfig, economyData, economyAccountsConfig, economyAccountsData);
        economyService = new EconomyService(economyManager, playerEconomyComponentType);
        this.getLogger().at(Level.INFO).log("Loaded " + economyAccountsData.getAccounts().size() + " economy accounts");
        this.getLogger().at(Level.INFO).log("TSEssentials command permissions require grants such as " +
                TSEssentialsPermissions.COMMAND_WILDCARD);

        moduleLoader = new ModuleLoader(
                ModulePaths.defaultModulesDirectory(this),
                getClassLoader(),
                getLogger(),
                this::createModuleContext
        );
        moduleLoader.discoverAndRegister();

        /* Initialize TPA request manager */
        teleportRequestManager = new TeleportRequestManager();

        /* Command Registry */
        this.getCommandRegistry().registerCommand(new SetHomeCommand("sethome", "Sets the users home at their current location!", this));
        this.getCommandRegistry().registerCommand(new HomeCommand("home", "Teleports user to their set home location!", this));

        this.getCommandRegistry().registerCommand(new SetWarpCommand("setwarp", "Creates a server-wide warp point!", this));
        this.getCommandRegistry().registerCommand(new WarpCommand("warp", "Teleports to a server warp location!", this));

        this.getCommandRegistry().registerCommand(new SetSpawnCommand("setspawn", "Sets the server spawn location!", this));
        this.getCommandRegistry().registerCommand(new SpawnCommand("spawn", "Teleports to the server spawn!", this));

        this.getCommandRegistry().registerCommand(new TpaCommand("tpa", "Request to teleport to another player", this));
        this.getCommandRegistry().registerCommand(new TpaHereCommand("tpahere", "Request another player to teleport to you", this));
        this.getCommandRegistry().registerCommand(new TpAcceptCommand("tpaccept", "Accept a pending teleport request", this));
        this.getCommandRegistry().registerCommand(new TpDenyCommand("tpdeny", "Deny a pending teleport request", this));
        this.getCommandRegistry().registerCommand(new TpHereCommand("tphere", "Force teleport a player to you (admin)", this));

        this.getCommandRegistry().registerCommand(new ChannelCommand(chatService));

        this.getCommandRegistry().registerCommand(new ChannelMessageCommand("g", "Send a global chat message", ChannelManager.GLOBAL, chatService));
        this.getCommandRegistry().registerCommand(new ChannelMessageCommand("l", "Send a local chat message", ChannelManager.LOCAL, chatService));
        this.getCommandRegistry().registerCommand(new ChannelMessageCommand("sc", "Send a staff chat message", ChannelManager.STAFF, chatService));
        this.getCommandRegistry().registerCommand(new IgnoreCommand("ignore", "Ignore a player's chat messages", true, chatService));
        this.getCommandRegistry().registerCommand(new IgnoreCommand("unignore", "Stop ignoring a player's chat messages", false, chatService));

        this.getCommandRegistry().registerCommand(new NickCommand(chatService));
        this.getCommandRegistry().registerCommand(new NickColorCommand(chatService));
        this.getCommandRegistry().registerCommand(new ChatEditCommand(channelManager));

        this.getCommandRegistry().registerCommand(new BalanceCommand(economyService));
        this.getCommandRegistry().registerCommand(new PayCommand(economyService));
        this.getCommandRegistry().registerCommand(new WalletCommand(economyService));
        this.getCommandRegistry().registerCommand(new EcoCommand(economyService));
        this.getCommandRegistry().registerCommand(new EconAdminUICommand(economyService));

        /* Event Registry */
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            ExampleEvent.onPlayerReady(event);
            chatService.loadSettings(event.getPlayer().getPlayerRef());
            economyService.loadEconomy(event.getPlayer().getPlayerRef());
            moduleLoader.onPlayerReady(event.getPlayer().getPlayerRef());
        });
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, new ChatListener(chatService)::onPlayerChat);

        moduleLoader.enableAll();
    }

    @Override
    protected void shutdown() {
        if (moduleLoader != null) {
            moduleLoader.disableAll();
        }
    }

    public <T> Config<T> registerModuleConfig(String key, BuilderCodec<T> codec) {
        return withConfig(key, codec);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerModuleComponent(
            Class<T> type,
            String componentId,
            BuilderCodec<T> codec
    ) {
        return getEntityStoreRegistry().registerComponent(type, componentId, codec);
    }

    public void registerModuleCommand(AbstractCommand command) {
        getCommandRegistry().registerCommand(command);
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    private TSEModuleContext createModuleContext(Path jarPath, TSEModuleDescriptor descriptor) {
        Path dataDirectory = ModulePaths.dataDirectory(this, descriptor.id());
        try {
            Files.createDirectories(dataDirectory);
        } catch (Exception ex) {
            getLogger().at(Level.WARNING).log("Unable to create module data directory " + dataDirectory + ": " + ex.getMessage());
        }
        return new CoreModuleContext(jarPath, dataDirectory);
    }

    private final class CoreModuleContext implements TSEModuleContext {
        private final Path moduleJarPath;
        private final Path moduleDataDirectory;

        private CoreModuleContext(Path moduleJarPath, Path moduleDataDirectory) {
            this.moduleJarPath = moduleJarPath;
            this.moduleDataDirectory = moduleDataDirectory;
        }

        @Override
        public TSEssentials core() {
            return TSEssentials.this;
        }

        @Override
        public HytaleLogger logger() {
            return getLogger();
        }

        @Override
        public Path moduleJarPath() {
            return moduleJarPath;
        }

        @Override
        public Path moduleDataDirectory() {
            return moduleDataDirectory;
        }

        @Override
        public <T> Config<T> registerConfig(String key, BuilderCodec<T> codec) {
            Config<T> config = new Config<>(moduleDataDirectory, key, codec);
            try {
                config.load().join();
            } catch (RuntimeException ex) {
                getLogger().at(Level.WARNING).log("Unable to load module config '" + key +
                        "' from " + moduleDataDirectory + ": " + ex.getMessage());
                throw ex;
            }
            return config;
        }

        @Override
        public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerComponent(
                Class<T> type,
                String componentId,
                BuilderCodec<T> codec
        ) {
            return registerModuleComponent(type, componentId, codec);
        }

        @Override
        public void registerCommand(AbstractCommand command) {
            registerModuleCommand(command);
        }

        @Override
        public EventRegistry eventRegistry() {
            return getEventRegistry();
        }
    }

    /**
     * Get the player data configuration
     * @return The player data config instance
     */
    public PlayerDataConfig getPlayerData() {
        return playerData;
    }

    /**
     * Save the player data to disk
     */
    public void savePlayerData() {
        playerDataConfig.save().thenRun(() -> {
            getLogger().at(Level.INFO).log("Player data saved successfully");
        });
    }

    /**
     * Get the warp data configuration
     * @return The warp data config instance
     */
    public WarpConfig getWarpData() {
        return warpData;
    }

    /**
     * Save the warp data to disk
     */
    public void saveWarpData() {
        warpDataConfig.save().thenRun(() -> {
            getLogger().at(Level.INFO).log("Warp data saved successfully");
        });
    }

    /**
     * Get the spawn data configuration
     * @return The spawn data config instance
     */
    public SpawnConfig getSpawnData() {
        return spawnData;
    }

    /**
     * Save the spawn data to disk
     */
    public void saveSpawnData() {
        spawnDataConfig.save().thenRun(() -> {
            getLogger().at(Level.INFO).log("Spawn data saved successfully");
        });
    }

    /**
     * Get the teleport request manager
     * @return The teleport request manager instance
     */
    public TeleportRequestManager getTeleportRequestManager() {
        return teleportRequestManager;
    }

    public ChatChannelConfig getChatChannelData() {
        return chatChannelData;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ChatService getChatService() {
        return chatService;
    }

    public ComponentType<EntityStore, PlayerChatSettingsComponent> getPlayerChatSettingsComponentType() {
        return playerChatSettingsComponentType;
    }

    public HomeService getHomeService() {
        return homeService;
    }

    public ComponentType<EntityStore, PlayerHomesComponent> getPlayerHomesComponentType() {
        return playerHomesComponentType;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public ComponentType<EntityStore, PlayerEconomyComponent> getPlayerEconomyComponentType() {
        return playerEconomyComponentType;
    }
}
