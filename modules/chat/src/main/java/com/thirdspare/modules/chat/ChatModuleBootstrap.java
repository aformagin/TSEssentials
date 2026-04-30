package com.thirdspare.modules.chat;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.api.TSEModule;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;
import com.thirdspare.modules.api.TSEPlayerChatEventHandler;
import com.thirdspare.modules.chat.commands.ChannelCommand;
import com.thirdspare.modules.chat.commands.ChannelMessageCommand;
import com.thirdspare.modules.chat.commands.ChatEditCommand;
import com.thirdspare.modules.chat.commands.IgnoreCommand;
import com.thirdspare.modules.chat.commands.NickColorCommand;
import com.thirdspare.modules.chat.commands.NickCommand;
import com.thirdspare.modules.chat.component.PlayerChatSettingsComponent;
import com.thirdspare.modules.chat.data.ChatChannel;
import com.thirdspare.modules.chat.data.ChatChannelsConfig;
import com.thirdspare.modules.chat.events.ChatListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class ChatModuleBootstrap implements TSEModule, TSEPlayerChatEventHandler {
    private static final TSEModuleDescriptor DESCRIPTOR = new TSEModuleDescriptor(
            "chat",
            "TSEssentials Chat",
            "1.1.0-CHAT-SNAPSHOT",
            "1.1.0",
            ""
    );

    private ChatService chatService;

    @Override
    public TSEModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void register(TSEModuleContext context) {
        Config<ChatChannelsConfig> chatConfig = context.registerConfig("chat_channels", ChatChannelsConfig.CODEC);
        ChatChannelsConfig chatChannels = chatConfig.get();
        if (chatChannels == null) {
            chatChannels = new ChatChannelsConfig();
            chatConfig.save().join();
        }
        migrateLegacyChannelsIfNeeded(context, chatConfig, chatChannels);

        ComponentType<EntityStore, PlayerChatSettingsComponent> componentType = context.registerComponent(
                PlayerChatSettingsComponent.class,
                "TSEssentials_PlayerChatSettings",
                PlayerChatSettingsComponent.CODEC
        );

        ChannelManager channelManager = new ChannelManager(chatConfig, chatChannels);
        chatService = new ChatService(channelManager, componentType);

        context.registerCommand(new ChannelCommand(chatService));
        context.registerCommand(new ChannelMessageCommand("g", "Send a global chat message", ChannelManager.GLOBAL, chatService));
        context.registerCommand(new ChannelMessageCommand("l", "Send a local chat message", ChannelManager.LOCAL, chatService));
        context.registerCommand(new ChannelMessageCommand("sc", "Send a staff chat message", ChannelManager.STAFF, chatService));
        context.registerCommand(new IgnoreCommand("ignore", "Ignore a player's chat messages", true, chatService));
        context.registerCommand(new IgnoreCommand("unignore", "Stop ignoring a player's chat messages", false, chatService));
        context.registerCommand(new NickCommand(chatService));
        context.registerCommand(new NickColorCommand(chatService));
        context.registerCommand(new ChatEditCommand(channelManager));
        context.logger().at(Level.INFO).log("Registered optional TSE chat module with " +
                channelManager.getChannels().size() + " chat channels.");
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    @Override
    public void onPlayerReady(PlayerRef player) {
        if (chatService != null) {
            chatService.loadSettings(player);
        }
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (chatService != null) {
            new ChatListener(chatService).onPlayerChat(event);
        }
    }

    private void migrateLegacyChannelsIfNeeded(TSEModuleContext context,
                                               Config<ChatChannelsConfig> moduleConfig,
                                               ChatChannelsConfig moduleChannels) {
        if (moduleChannels != null && !moduleChannels.isEmpty()) {
            return;
        }

        Path legacyDataDirectory = context.moduleDataDirectory()
                .getParent()
                .getParent();
        if (legacyDataDirectory == null || !Files.exists(legacyDataDirectory.resolve("chat_channels.json"))) {
            return;
        }

        Config<ChatChannelsConfig> legacyConfig = new Config<>(legacyDataDirectory, "chat_channels", ChatChannelsConfig.CODEC);
        legacyConfig.load().join();
        ChatChannelsConfig legacyChannels = legacyConfig.get();
        if (legacyChannels == null || legacyChannels.isEmpty()) {
            return;
        }

        for (ChatChannel channel : legacyChannels.getAllChannels()) {
            moduleChannels.setChannel(channel.copy());
        }
        moduleChannels.normalizeKeys();
        moduleConfig.save().join();
        context.logger().at(Level.INFO).log("Migrated legacy core chat_channels.json into optional chat module storage.");
    }
}
