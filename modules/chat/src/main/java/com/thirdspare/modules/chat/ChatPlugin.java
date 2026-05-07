package com.thirdspare.modules.chat;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.api.TSEssentialsApi;
import com.thirdspare.modules.chat.commands.ChannelCommand;
import com.thirdspare.modules.chat.commands.ChannelMessageCommand;
import com.thirdspare.modules.chat.commands.ChatEditCommand;
import com.thirdspare.modules.chat.commands.IgnoreCommand;
import com.thirdspare.modules.chat.commands.NickColorCommand;
import com.thirdspare.modules.chat.commands.NickCommand;
import com.thirdspare.modules.chat.component.PlayerChatSettingsComponent;
import com.thirdspare.modules.chat.data.ChatChannelsConfig;
import com.thirdspare.modules.chat.events.ChatListener;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class ChatPlugin extends JavaPlugin {
    private static final String CHAT_EDIT_UI = "ChatEdit.ui";

    private final Config<ChatChannelsConfig> chatConfig = withConfig("chat_channels", ChatChannelsConfig.CODEC);
    private ChatService chatService;

    public ChatPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        TSEssentialsApi.registerPermissionNodes(TSEChatPermissionsNodes.permissionNodes());

        ChatChannelsConfig chatChannels = chatConfig.get();
        if (chatChannels == null) {
            chatChannels = new ChatChannelsConfig();
            chatConfig.save().join();
        }

        ComponentType<EntityStore, PlayerChatSettingsComponent> componentType = getEntityStoreRegistry()
                .registerComponent(
                        PlayerChatSettingsComponent.class,
                        "TSEssentials_PlayerChatSettings",
                        PlayerChatSettingsComponent.CODEC
                );

        ChannelManager channelManager = new ChannelManager(chatConfig, chatChannels);
        chatService = new ChatService(channelManager, componentType);

        getCommandRegistry().registerCommand(new ChannelCommand(chatService));
        getCommandRegistry().registerCommand(new ChannelMessageCommand("g", "Send a global chat message", ChannelManager.GLOBAL, chatService));
        getCommandRegistry().registerCommand(new ChannelMessageCommand("l", "Send a local chat message", ChannelManager.LOCAL, chatService));
        getCommandRegistry().registerCommand(new ChannelMessageCommand("sc", "Send a staff chat message", ChannelManager.STAFF, chatService));
        getCommandRegistry().registerCommand(new IgnoreCommand("ignore", "Ignore a player's chat messages", true, chatService));
        getCommandRegistry().registerCommand(new IgnoreCommand("unignore", "Stop ignoring a player's chat messages", false, chatService));
        getCommandRegistry().registerCommand(new NickCommand(chatService));
        getCommandRegistry().registerCommand(new NickColorCommand(chatService));
        getCommandRegistry().registerCommand(new ChatEditCommand(channelManager, CHAT_EDIT_UI));

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> chatService.loadSettings(event.getPlayer().getPlayerRef()));
        ChatListener listener = new ChatListener(chatService);
        getEventRegistry().registerGlobal(PlayerChatEvent.class, listener::onPlayerChat);

        getLogger().at(Level.INFO).log("Registered TSEssentials Chat plugin with " +
                channelManager.getChannels().size() + " chat channels.");
    }
}
