package com.thirdspare.chat;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.data.chat.ChatChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatService {
    private static final String ERROR_COLOR = "#FF6B6B";
    private static final String SUCCESS_COLOR = "#8DE969";

    private final ChannelManager channelManager;
    private final ComponentType<EntityStore, PlayerChatSettingsComponent> settingsComponentType;
    private final ConcurrentHashMap<UUID, PlayerChatSettingsComponent> runtimeSettings = new ConcurrentHashMap<>();

    public ChatService(ChannelManager channelManager,
                       ComponentType<EntityStore, PlayerChatSettingsComponent> settingsComponentType) {
        this.channelManager = channelManager;
        this.settingsComponentType = settingsComponentType;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public PlayerChatSettingsComponent getSettings(PlayerRef player) {
        PlayerChatSettingsComponent settings = runtimeSettings.computeIfAbsent(
                player.getUuid(),
                ignored -> new PlayerChatSettingsComponent()
        );
        synchronized (settings) {
            repairSettings(player, settings);
        }
        return settings;
    }

    public void loadSettings(PlayerRef player) {
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                Ref<EntityStore> ref = player.getReference();
                Store<EntityStore> store = ref.getStore();
                PlayerChatSettingsComponent storedSettings = store.ensureAndGetComponent(ref, settingsComponentType);
                repairSettings(player, storedSettings);
                runtimeSettings.put(player.getUuid(), new PlayerChatSettingsComponent(storedSettings));
            } catch (IllegalStateException ignored) {
                runtimeSettings.computeIfAbsent(player.getUuid(), ignoredUuid -> new PlayerChatSettingsComponent());
            }
        });
    }

    public ChatChannel resolveFocusChannel(PlayerRef sender) {
        PlayerChatSettingsComponent settings = getSettings(sender);
        ChatChannel channel = channelManager.getChannel(settings.getFocusChannel());
        if (channel == null || !canUseChannel(sender, channel)) {
            channel = channelManager.getGlobalChannel();
            synchronized (settings) {
                settings.setFocusChannel(channel.getName());
                settings.subscribe(channel.getName());
            }
            saveSettings(sender, settings);
        }
        return channel;
    }

    public boolean canUseChannel(PlayerRef player, ChatChannel channel) {
        return channel != null && (!channel.hasPermissionNode() || hasPermission(player, channel.getPermission()));
    }

    public List<PlayerRef> filterRecipients(PlayerRef sender, Collection<PlayerRef> candidates, ChatChannel channel) {
        List<PlayerRef> recipients = new ArrayList<>();
        if (channel == null || !canUseChannel(sender, channel)) {
            return recipients;
        }

        for (PlayerRef target : candidates) {
            if (target == null || !target.isValid()) {
                continue;
            }
            if (!canUseChannel(target, channel)) {
                continue;
            }
            PlayerChatSettingsComponent targetSettings = getSettings(target);
            synchronized (targetSettings) {
                if (!targetSettings.isSubscribed(channel.getName())) {
                    continue;
                }
                if (targetSettings.ignores(sender.getUuid())) {
                    continue;
                }
            }
            if (!isWithinRange(sender, target, channel)) {
                continue;
            }
            recipients.add(target);
        }
        return recipients;
    }

    public void sendDirect(PlayerRef sender, String channelName, String content) {
        ChatChannel channel = channelManager.getChannel(channelName);
        if (channel == null) {
            sender.sendMessage(Message.raw("Unknown channel: " + channelName).color(ERROR_COLOR));
            return;
        }
        if (!canUseChannel(sender, channel)) {
            sender.sendMessage(Message.raw("You do not have permission to use " + channel.getName() + ".").color(ERROR_COLOR));
            return;
        }

        List<PlayerRef> recipients = filterRecipients(sender, Universe.get().getPlayers(), channel);
        Message message = format(channel, sender, content);
        for (PlayerRef recipient : recipients) {
            recipient.sendMessage(message);
        }
        if (recipients.isEmpty()) {
            sender.sendMessage(Message.raw("No one can hear you in " + channel.getName() + ".").color(ERROR_COLOR));
        }
    }

    public Message format(ChatChannel channel, PlayerRef sender, String content) {
        String displayName = getDisplayName(sender);
        Message prefix = Message.raw(channel.getPrefix() + " ").color(channel.getColor());
        Message name = Message.raw(displayName + ": ").color(getDisplayColor(sender));
        Message body = Message.raw(content).color("#FFFFFF");
        return Message.join(prefix, name, body);
    }

    public void focus(PlayerRef player, String channelName) {
        ChatChannel channel = channelManager.getChannel(channelName);
        if (channel == null) {
            player.sendMessage(Message.raw("Unknown channel: " + channelName).color(ERROR_COLOR));
            return;
        }
        if (!canUseChannel(player, channel)) {
            player.sendMessage(Message.raw("You do not have permission to use " + channel.getName() + ".").color(ERROR_COLOR));
            return;
        }
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            settings.subscribe(channel.getName());
            settings.setFocusChannel(channel.getName());
        }
        saveSettings(player, settings);
        player.sendMessage(Message.raw("Focus channel set to " + channel.getName() + ".").color(SUCCESS_COLOR));
    }

    public void join(PlayerRef player, String channelName) {
        ChatChannel channel = channelManager.getChannel(channelName);
        if (channel == null) {
            player.sendMessage(Message.raw("Unknown channel: " + channelName).color(ERROR_COLOR));
            return;
        }
        if (!canUseChannel(player, channel)) {
            player.sendMessage(Message.raw("You do not have permission to join " + channel.getName() + ".").color(ERROR_COLOR));
            return;
        }
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            settings.subscribe(channel.getName());
        }
        saveSettings(player, settings);
        player.sendMessage(Message.raw("Joined " + channel.getName() + ".").color(SUCCESS_COLOR));
    }

    public void leave(PlayerRef player, String channelName) {
        String normalized = ChatChannel.normalizeName(channelName);
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            if (ChannelManager.GLOBAL.equals(normalized)) {
                player.sendMessage(Message.raw("You cannot leave global chat.").color(ERROR_COLOR));
                return;
            }
            settings.unsubscribe(normalized);
            if (normalized.equals(settings.getFocusChannel())) {
                settings.setFocusChannel(ChannelManager.GLOBAL);
                settings.subscribe(ChannelManager.GLOBAL);
            }
        }
        saveSettings(player, settings);
        player.sendMessage(Message.raw("Left " + normalized + ".").color(SUCCESS_COLOR));
    }

    public void listChannels(PlayerRef player) {
        PlayerChatSettingsComponent settings = getSettings(player);
        Message message = Message.raw("Channels: ").color("#FFFFFF");
        for (ChatChannel channel : channelManager.getChannels()) {
            if (!canUseChannel(player, channel)) {
                continue;
            }
            String marker;
            String listening;
            synchronized (settings) {
                marker = channel.getName().equals(settings.getFocusChannel()) ? "*" : "";
                listening = settings.isSubscribed(channel.getName()) ? "+" : "-";
            }
            message.insert(Message.raw(listening + marker + channel.getName() + " ").color(channel.getColor()));
        }
        player.sendMessage(message);
    }

    public void ignore(PlayerRef player, PlayerRef target) {
        if (target.getUuid().equals(player.getUuid())) {
            player.sendMessage(Message.raw("You cannot ignore yourself.").color(ERROR_COLOR));
            return;
        }
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            settings.ignore(target.getUuid());
        }
        saveSettings(player, settings);
        player.sendMessage(Message.raw("Ignoring " + target.getUsername() + ".").color(SUCCESS_COLOR));
    }

    public void unignore(PlayerRef player, PlayerRef target) {
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            settings.unignore(target.getUuid());
        }
        saveSettings(player, settings);
        player.sendMessage(Message.raw("No longer ignoring " + target.getUsername() + ".").color(SUCCESS_COLOR));
    }

    public void setNickname(PlayerRef player, String nickname) {
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            if (nickname == null || nickname.isBlank() || nickname.equalsIgnoreCase("off")) {
                settings.setNickname("");
                saveSettings(player, settings);
                player.sendMessage(Message.raw("Nickname cleared.").color(SUCCESS_COLOR));
                return;
            }
            String trimmed = nickname.trim();
            if (trimmed.length() > 24) {
                player.sendMessage(Message.raw("Nicknames must be 24 characters or fewer.").color(ERROR_COLOR));
                return;
            }
            settings.setNickname(trimmed);
            saveSettings(player, settings);
            player.sendMessage(Message.raw("Nickname set to " + trimmed + ".").color(SUCCESS_COLOR));
        }
    }

    public void setNicknameColor(PlayerRef player, String color) {
        PlayerChatSettingsComponent settings = getSettings(player);
        synchronized (settings) {
            if (color == null || color.isBlank() || color.equalsIgnoreCase("off")) {
                settings.setNicknameColor("");
                saveSettings(player, settings);
                player.sendMessage(Message.raw("Nickname color cleared.").color(SUCCESS_COLOR));
                return;
            }
            if (!ChannelManager.isValidColor(color)) {
                player.sendMessage(Message.raw("Colors must use #RRGGBB format.").color(ERROR_COLOR));
                return;
            }
            settings.setNicknameColor(color);
            saveSettings(player, settings);
            player.sendMessage(Message.raw("Nickname color set.").color(SUCCESS_COLOR));
        }
    }

    public void repairSettings(PlayerRef player, PlayerChatSettingsComponent settings) {
        if (settings.getFocusChannel() == null || settings.getFocusChannel().isBlank()) {
            settings.setFocusChannel(ChannelManager.GLOBAL);
        }
        for (ChatChannel channel : channelManager.getChannels()) {
            if (channel.isDefaultSubscribed() && canUseChannel(player, channel)) {
                settings.subscribe(channel.getName());
            }
        }
        ChatChannel focus = channelManager.getChannel(settings.getFocusChannel());
        if (focus == null || !canUseChannel(player, focus)) {
            settings.setFocusChannel(ChannelManager.GLOBAL);
            settings.subscribe(ChannelManager.GLOBAL);
        }
    }

    private boolean isWithinRange(PlayerRef sender, PlayerRef target, ChatChannel channel) {
        if (!channel.isRanged()) {
            return true;
        }
        UUID senderWorld = sender.getWorldUuid();
        UUID targetWorld = target.getWorldUuid();
        if (senderWorld == null || targetWorld == null || !senderWorld.equals(targetWorld)) {
            return false;
        }
        var senderPosition = sender.getTransform().getPosition();
        var targetPosition = target.getTransform().getPosition();
        double dx = senderPosition.x() - targetPosition.x();
        double dy = senderPosition.y() - targetPosition.y();
        double dz = senderPosition.z() - targetPosition.z();
        return (dx * dx) + (dy * dy) + (dz * dz) <= channel.getRange() * channel.getRange();
    }

    private String getDisplayName(PlayerRef player) {
        PlayerChatSettingsComponent settings = getSettings(player);
        String nickname;
        synchronized (settings) {
            nickname = settings.getNickname();
        }
        return nickname == null || nickname.isBlank() ? player.getUsername() : nickname;
    }

    private String getDisplayColor(PlayerRef player) {
        PlayerChatSettingsComponent settings = getSettings(player);
        String color;
        synchronized (settings) {
            color = settings.getNicknameColor();
        }
        return color == null || color.isBlank() ? "#FFFFFF" : color;
    }

    private void saveSettings(PlayerRef player, PlayerChatSettingsComponent settings) {
        PlayerChatSettingsComponent snapshot;
        synchronized (settings) {
            snapshot = new PlayerChatSettingsComponent(settings);
        }
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                Ref<EntityStore> ref = player.getReference();
                Store<EntityStore> store = ref.getStore();
                PlayerChatSettingsComponent storedSettings = store.ensureAndGetComponent(ref, settingsComponentType);
                copySettings(snapshot, storedSettings);
            } catch (IllegalStateException ignored) {
                // The player can leave before a queued save runs; runtime settings already hold the latest value.
            }
        });
    }

    private void copySettings(PlayerChatSettingsComponent source, PlayerChatSettingsComponent target) {
        target.setFocusChannel(source.getFocusChannel());
        target.getSubscribedChannels().clear();
        target.getSubscribedChannels().addAll(source.getSubscribedChannels());
        target.getIgnoredPlayerUuids().clear();
        target.getIgnoredPlayerUuids().addAll(source.getIgnoredPlayerUuids());
        target.setNickname(source.getNickname());
        target.setNicknameColor(source.getNicknameColor());
    }

    private boolean hasPermission(PlayerRef player, String permission) {
        try {
            return (boolean) player.getClass()
                    .getMethod("hasPermission", String.class)
                    .invoke(player, permission);
        } catch (ReflectiveOperationException ignored) {
            try {
                return (boolean) player.getClass()
                        .getMethod("hasPermission", String.class, boolean.class)
                        .invoke(player, permission, false);
            } catch (ReflectiveOperationException ignoredAgain) {
                return false;
            }
        }
    }
}
