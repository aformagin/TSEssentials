package com.thirdspare.chat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.data.chat.ChatChannel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent per-player chat preferences.
 */
public class PlayerChatSettingsComponent implements Component<EntityStore> {
    public static final String DEFAULT_FOCUS = "global";

    private String focusChannel;
    private Set<String> subscribedChannels;
    private Set<UUID> ignoredPlayerUuids;
    private String nickname;
    private String nicknameColor;

    public PlayerChatSettingsComponent() {
        this.focusChannel = DEFAULT_FOCUS;
        this.subscribedChannels = new HashSet<>();
        this.ignoredPlayerUuids = new HashSet<>();
        this.nickname = "";
        this.nicknameColor = "";
    }

    public PlayerChatSettingsComponent(PlayerChatSettingsComponent other) {
        this.focusChannel = other.focusChannel;
        this.subscribedChannels = new HashSet<>(other.subscribedChannels);
        this.ignoredPlayerUuids = new HashSet<>(other.ignoredPlayerUuids);
        this.nickname = other.nickname;
        this.nicknameColor = other.nicknameColor;
    }

    @Override
    public Component<EntityStore> clone() {
        return new PlayerChatSettingsComponent(this);
    }

    public String getFocusChannel() {
        return focusChannel;
    }

    public void setFocusChannel(String focusChannel) {
        this.focusChannel = ChatChannel.normalizeName(focusChannel);
    }

    public Set<String> getSubscribedChannels() {
        return subscribedChannels;
    }

    public boolean isSubscribed(String channelName) {
        return subscribedChannels.contains(ChatChannel.normalizeName(channelName));
    }

    public void subscribe(String channelName) {
        subscribedChannels.add(ChatChannel.normalizeName(channelName));
    }

    public void unsubscribe(String channelName) {
        subscribedChannels.remove(ChatChannel.normalizeName(channelName));
    }

    public Set<UUID> getIgnoredPlayerUuids() {
        return ignoredPlayerUuids;
    }

    public boolean ignores(UUID uuid) {
        return ignoredPlayerUuids.contains(uuid);
    }

    public void ignore(UUID uuid) {
        ignoredPlayerUuids.add(uuid);
    }

    public void unignore(UUID uuid) {
        ignoredPlayerUuids.remove(uuid);
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname != null ? nickname.trim() : "";
    }

    public String getNicknameColor() {
        return nicknameColor;
    }

    public void setNicknameColor(String nicknameColor) {
        this.nicknameColor = nicknameColor != null ? nicknameColor.trim() : "";
    }

    public static final BuilderCodec<PlayerChatSettingsComponent> CODEC =
            BuilderCodec.builder(PlayerChatSettingsComponent.class, PlayerChatSettingsComponent::new)
                    .append(new KeyedCodec<>("FocusChannel", Codec.STRING),
                            (settings, value) -> settings.focusChannel = ChatChannel.normalizeName(value),
                            settings -> settings.focusChannel).add()
                    .append(new KeyedCodec<>("SubscribedChannels",
                            new SetCodec<>(Codec.STRING, HashSet::new, false)),
                            (settings, value) -> settings.subscribedChannels = normalizeChannelSet(value),
                            settings -> settings.subscribedChannels).add()
                    .append(new KeyedCodec<>("IgnoredPlayerUuids",
                            new SetCodec<>(Codec.UUID_STRING, HashSet::new, false)),
                            (settings, value) -> settings.ignoredPlayerUuids = value != null ? new HashSet<>(value) : new HashSet<>(),
                            settings -> settings.ignoredPlayerUuids).add()
                    .append(new KeyedCodec<>("Nickname", Codec.STRING),
                            (settings, value) -> settings.nickname = value != null ? value : "",
                            settings -> settings.nickname).add()
                    .append(new KeyedCodec<>("NicknameColor", Codec.STRING),
                            (settings, value) -> settings.nicknameColor = value != null ? value : "",
                            settings -> settings.nicknameColor).add()
                    .build();

    private static Set<String> normalizeChannelSet(Set<String> values) {
        Set<String> normalized = new HashSet<>();
        if (values != null) {
            for (String value : values) {
                String channel = ChatChannel.normalizeName(value);
                if (!channel.isBlank()) {
                    normalized.add(channel);
                }
            }
        }
        return normalized;
    }
}
