package com.thirdspare.modules.chat.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Codec-backed JSON config for server-wide chat channels.
 */
public class ChatChannelsConfig {
    private int schemaVersion;
    private Map<String, ChatChannel> channels;

    public ChatChannelsConfig() {
        this.schemaVersion = 1;
        this.channels = new HashMap<>();
    }

    public ChatChannelsConfig(Map<String, ChatChannel> channels) {
        this.schemaVersion = 1;
        this.channels = channels != null ? new HashMap<>(channels) : new HashMap<>();
        normalizeKeys();
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = Math.max(1, schemaVersion);
    }

    public Map<String, ChatChannel> getChannels() {
        return channels;
    }

    public Collection<ChatChannel> getAllChannels() {
        return channels.values();
    }

    public ChatChannel getChannel(String name) {
        return channels.get(ChatChannel.normalizeName(name));
    }

    public boolean hasChannel(String name) {
        return channels.containsKey(ChatChannel.normalizeName(name));
    }

    public void setChannel(ChatChannel channel) {
        if (channel == null || channel.getName().isBlank()) {
            return;
        }
        channels.put(ChatChannel.normalizeName(channel.getName()), channel);
    }

    public boolean removeChannel(String name) {
        return channels.remove(ChatChannel.normalizeName(name)) != null;
    }

    public boolean isEmpty() {
        return channels == null || channels.isEmpty();
    }

    public void normalizeKeys() {
        Map<String, ChatChannel> normalized = new HashMap<>();
        if (channels != null) {
            for (ChatChannel channel : channels.values()) {
                if (channel != null && !channel.getName().isBlank()) {
                    normalized.put(ChatChannel.normalizeName(channel.getName()), channel);
                }
            }
        }
        channels = normalized;
    }

    public static final BuilderCodec<ChatChannelsConfig> CODEC = BuilderCodec.builder(ChatChannelsConfig.class, ChatChannelsConfig::new)
            .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER),
                    ChatChannelsConfig::setSchemaVersion,
                    ChatChannelsConfig::getSchemaVersion).add()
            .append(new KeyedCodec<>("Channels",
                    new ObjectMapCodec<>(
                            ChatChannel.CODEC,
                            HashMap::new,
                            key -> key,
                            str -> str
                    )),
                    (config, value) -> {
                        config.channels = value != null ? new HashMap<>(value) : new HashMap<>();
                        config.normalizeKeys();
                    },
                    config -> config.channels).add()
            .build();
}
