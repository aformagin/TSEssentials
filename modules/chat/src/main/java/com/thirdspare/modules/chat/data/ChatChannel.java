package com.thirdspare.modules.chat.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Server-wide chat channel definition.
 */
public class ChatChannel {
    private String name;
    private String prefix;
    private String color;
    private boolean ranged;
    private double range;
    private String permission;
    private boolean defaultSubscribed;

    public ChatChannel() {
        this.name = "";
        this.prefix = "";
        this.color = "#FFFFFF";
        this.ranged = false;
        this.range = 0.0D;
        this.permission = "";
        this.defaultSubscribed = false;
    }

    public ChatChannel(String name, String prefix, String color, boolean ranged,
                       double range, String permission, boolean defaultSubscribed) {
        this.name = normalizeName(name);
        this.prefix = prefix != null ? prefix : "";
        this.color = color != null && !color.isBlank() ? color : "#FFFFFF";
        this.ranged = ranged;
        this.range = Math.max(0.0D, range);
        this.permission = permission != null ? permission : "";
        this.defaultSubscribed = defaultSubscribed;
    }

    public static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeName(name);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color != null && !color.isBlank() ? color : "#FFFFFF";
    }

    public boolean isRanged() {
        return ranged;
    }

    public void setRanged(boolean ranged) {
        this.ranged = ranged;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = Math.max(0.0D, range);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission != null ? permission : "";
    }

    public boolean hasPermissionNode() {
        return permission != null && !permission.isBlank();
    }

    public boolean isDefaultSubscribed() {
        return defaultSubscribed;
    }

    public void setDefaultSubscribed(boolean defaultSubscribed) {
        this.defaultSubscribed = defaultSubscribed;
    }

    public ChatChannel copy() {
        return new ChatChannel(name, prefix, color, ranged, range, permission, defaultSubscribed);
    }

    public static final BuilderCodec<ChatChannel> CODEC = BuilderCodec.builder(ChatChannel.class, ChatChannel::new)
            .append(new KeyedCodec<>("Name", Codec.STRING),
                    (channel, value) -> channel.name = normalizeName(value),
                    channel -> channel.name).add()
            .append(new KeyedCodec<>("Prefix", Codec.STRING),
                    (channel, value) -> channel.prefix = value != null ? value : "",
                    channel -> channel.prefix).add()
            .append(new KeyedCodec<>("Color", Codec.STRING),
                    (channel, value) -> channel.color = value != null && !value.isBlank() ? value : "#FFFFFF",
                    channel -> channel.color).add()
            .append(new KeyedCodec<>("Ranged", Codec.BOOLEAN),
                    (channel, value) -> channel.ranged = value,
                    channel -> channel.ranged).add()
            .append(new KeyedCodec<>("Range", Codec.DOUBLE),
                    (channel, value) -> channel.range = Math.max(0.0D, value),
                    channel -> channel.range).add()
            .append(new KeyedCodec<>("Permission", Codec.STRING),
                    (channel, value) -> channel.permission = value != null ? value : "",
                    channel -> channel.permission).add()
            .append(new KeyedCodec<>("DefaultSubscribed", Codec.BOOLEAN),
                    (channel, value) -> channel.defaultSubscribed = value,
                    channel -> channel.defaultSubscribed).add()
            .build();
}
