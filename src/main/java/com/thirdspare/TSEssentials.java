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
import com.thirdspare.commands.core.*;
import com.thirdspare.core.back.BackService;
import com.thirdspare.core.flight.FlightService;
import com.thirdspare.core.kits.KitManager;
import com.thirdspare.core.kits.KitService;
import com.thirdspare.core.kits.data.KitsConfig;
import com.thirdspare.core.motd.MotdManager;
import com.thirdspare.core.motd.data.MotdConfig;
import com.thirdspare.core.nearby.NearbyService;
import com.thirdspare.core.position.PositionService;
import com.thirdspare.core.rules.RulesManager;
import com.thirdspare.core.rules.data.RulesConfig;
import com.thirdspare.core.teleport.TeleportAllService;
import com.thirdspare.core.homes.HomeService;
import com.thirdspare.core.homes.PlayerHomesComponent;
import com.thirdspare.core.homes.data.PlayerDataConfig;
import com.thirdspare.core.spawn.data.SpawnConfig;
import com.thirdspare.core.tpa.TeleportRequestManager;
import com.thirdspare.core.warps.data.WarpConfig;
import com.thirdspare.events.ExampleEvent;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;
import com.thirdspare.modules.core.ModuleLoader;
import com.thirdspare.modules.core.ModulePaths;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.Teleportation;

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

    private final Config<MotdConfig> motdConfig = withConfig("motd_config", MotdConfig.CODEC);
    private final Config<RulesConfig> rulesConfig = withConfig("rules_config", RulesConfig.CODEC);
    private final Config<KitsConfig> kitsConfig = withConfig("kits_config", KitsConfig.CODEC);

    private PlayerDataConfig playerData;
    private WarpConfig warpData;
    private SpawnConfig spawnData;
    private ComponentType<EntityStore, PlayerHomesComponent> playerHomesComponentType;
    private HomeService homeService;
    private MotdManager motdManager;
    private RulesManager rulesManager;
    private KitManager kitManager;
    private KitService kitService;
    private NearbyService nearbyService;
    private PositionService positionService;
    private FlightService flightService;
    private BackService backService;
    private TeleportAllService teleportAllService;
    private TeleportRequestManager teleportRequestManager;
    private ModuleLoader moduleLoader;

    public TSEssentials(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        playerHomesComponentType = this.getEntityStoreRegistry()
                .registerComponent(
                        PlayerHomesComponent.class,
                        "TSEssentials_PlayerHomes",
                        PlayerHomesComponent.CODEC
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

        this.getLogger().at(Level.INFO).log("TSEssentials command permissions require grants such as " +
                TSEssentialsPermissions.COMMAND_WILDCARD);

        motdManager = new MotdManager(motdConfig, motdConfig.get());
        rulesManager = new RulesManager(rulesConfig, rulesConfig.get());
        kitManager = new KitManager(kitsConfig, kitsConfig.get());
        kitService = new KitService(kitManager);
        nearbyService = new NearbyService();
        positionService = new PositionService();
        flightService = new FlightService();
        backService = new BackService();
        teleportAllService = new TeleportAllService();
        Teleportation.setBackService(backService);

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
        this.getCommandRegistry().registerCommand(new HealCommand());
        this.getCommandRegistry().registerCommand(new RepairCommand());
        this.getCommandRegistry().registerCommand(new RepairAllCommand());
        this.getCommandRegistry().registerCommand(new MotdCommand(motdManager));
        this.getCommandRegistry().registerCommand(new MotdAdminCommand(motdManager));
        this.getCommandRegistry().registerCommand(new NearbyCommand(nearbyService));
        this.getCommandRegistry().registerCommand(new KitCommand(kitService));
        this.getCommandRegistry().registerCommand(new KitAdminCommand(kitService));
        this.getCommandRegistry().registerCommand(new GetPosCommand(positionService));
        this.getCommandRegistry().registerCommand(new RulesCommand(rulesManager));
        this.getCommandRegistry().registerCommand(new RulesAdminCommand(rulesManager));
        this.getCommandRegistry().registerCommand(new FlyCommand(flightService));
        this.getCommandRegistry().registerCommand(new BackCommand(backService));
        this.getCommandRegistry().registerCommand(new TpAllCommand(teleportAllService));

        /* Event Registry */
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            ExampleEvent.onPlayerReady(event);
            motdManager.sendTo(event.getPlayer().getPlayerRef(), false);
            moduleLoader.onPlayerReady(event.getPlayer().getPlayerRef());
        });
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, event -> moduleLoader.onPlayerChat(event));

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

    public HomeService getHomeService() {
        return homeService;
    }

    public ComponentType<EntityStore, PlayerHomesComponent> getPlayerHomesComponentType() {
        return playerHomesComponentType;
    }

}
