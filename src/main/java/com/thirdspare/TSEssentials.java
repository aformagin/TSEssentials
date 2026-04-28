package com.thirdspare;

import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.component.ComponentType;
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
import com.thirdspare.data.chat.ChatChannelConfig;
import com.thirdspare.data.PlayerDataConfig;
import com.thirdspare.data.SpawnConfig;
import com.thirdspare.data.WarpConfig;
import com.thirdspare.events.ExampleEvent;
import com.thirdspare.events.chat.ChatListener;
import com.thirdspare.homes.HomeService;
import com.thirdspare.homes.PlayerHomesComponent;
import com.thirdspare.tpa.TeleportRequestManager;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class TSEssentials extends JavaPlugin {

    /* Player data configuration with codec-based JSON persistence */
    private final Config<PlayerDataConfig> playerDataConfig = withConfig("player_data", PlayerDataConfig.CODEC);

    /* Warp data configuration with codec-based JSON persistence */
    private final Config<WarpConfig> warpDataConfig = withConfig("warp_data", WarpConfig.CODEC);

    /* Spawn data configuration with codec-based JSON persistence */
    private final Config<SpawnConfig> spawnDataConfig = withConfig("spawn_data", SpawnConfig.CODEC);

    /* Chat channel configuration with codec-based JSON persistence */
    private final Config<ChatChannelConfig> chatChannelConfig = withConfig("chat_channels", ChatChannelConfig.CODEC);

    private PlayerDataConfig playerData;
    private WarpConfig warpData;
    private SpawnConfig spawnData;
    private ChatChannelConfig chatChannelData;
    private ComponentType<EntityStore, PlayerChatSettingsComponent> playerChatSettingsComponentType;
    private ComponentType<EntityStore, PlayerHomesComponent> playerHomesComponentType;
    private ChannelManager channelManager;
    private ChatService chatService;
    private HomeService homeService;
    private TeleportRequestManager teleportRequestManager;

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

        /* Event Registry */
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            ExampleEvent.onPlayerReady(event);
            chatService.loadSettings(event.getPlayer().getPlayerRef());
        });
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, new ChatListener(chatService)::onPlayerChat);
    }

    /**
     * Get the player data configuration
     *
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
     *
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
     *
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
     *
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
}
