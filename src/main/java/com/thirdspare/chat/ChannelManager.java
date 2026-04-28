package com.thirdspare.chat;

import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.data.chat.ChatChannel;
import com.thirdspare.data.chat.ChatChannelConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ChannelManager {
    public static final String GLOBAL = "global";
    public static final String LOCAL = "local";
    public static final String STAFF = "staff";
    public static final String STAFF_PERMISSION = "tsessentials.chat.staff";

    private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private final Config<ChatChannelConfig> config;
    private final ChatChannelConfig channels;

    public ChannelManager(Config<ChatChannelConfig> config, ChatChannelConfig channels) {
        this.config = config;
        this.channels = channels;
        if (ensureDefaults()) {
            save();
        }
    }

    public ChatChannel getChannel(String name) {
        return channels.getChannel(name);
    }

    public boolean hasChannel(String name) {
        return channels.hasChannel(name);
    }

    public List<ChatChannel> getChannels() {
        List<ChatChannel> result = new ArrayList<>(channels.getAllChannels());
        result.sort(Comparator.comparing(ChatChannel::getName));
        return result;
    }

    public ChatChannel getGlobalChannel() {
        return channels.getChannel(GLOBAL);
    }

    public boolean ensureDefaults() {
        boolean changed = false;
        channels.normalizeKeys();
        if (!channels.hasChannel(GLOBAL)) {
            channels.setChannel(new ChatChannel(GLOBAL, "[G]", "#E6E6E6", false, 0.0D, "", true));
            changed = true;
        }
        if (!channels.hasChannel(LOCAL)) {
            channels.setChannel(new ChatChannel(LOCAL, "[L]", "#8DE969", true, 100.0D, "", true));
            changed = true;
        }
        if (!channels.hasChannel(STAFF)) {
            channels.setChannel(new ChatChannel(STAFF, "[Staff]", "#FFB347", false, 0.0D, STAFF_PERMISSION, false));
            changed = true;
        }
        return changed;
    }

    public CompletableFuture<Void> save() {
        channels.normalizeKeys();
        return config.save();
    }

    public String createOrUpdate(String name, String prefix, String color, boolean ranged, double range,
                                 String permission, boolean defaultSubscribed) {
        String normalized = ChatChannel.normalizeName(name);
        if (!isValidChannelName(normalized)) {
            return "Channel names may only contain letters, numbers, underscores, and hyphens.";
        }
        if (!isValidColor(color)) {
            return "Colors must use #RRGGBB format.";
        }
        channels.setChannel(new ChatChannel(normalized, prefix, color, ranged, range, permission, defaultSubscribed));
        save();
        return null;
    }

    public String delete(String name) {
        String normalized = ChatChannel.normalizeName(name);
        if (GLOBAL.equals(normalized)) {
            return "The global channel cannot be deleted.";
        }
        if (!channels.removeChannel(normalized)) {
            return "Unknown channel: " + normalized;
        }
        save();
        return null;
    }

    public String setPrefix(String name, String prefix) {
        ChatChannel channel = getChannel(name);
        if (channel == null) {
            return "Unknown channel: " + name;
        }
        channel.setPrefix(prefix);
        save();
        return null;
    }

    public String setColor(String name, String color) {
        ChatChannel channel = getChannel(name);
        if (channel == null) {
            return "Unknown channel: " + name;
        }
        if (!isValidColor(color)) {
            return "Colors must use #RRGGBB format.";
        }
        channel.setColor(color);
        save();
        return null;
    }

    public String setRange(String name, double range) {
        ChatChannel channel = getChannel(name);
        if (channel == null) {
            return "Unknown channel: " + name;
        }
        channel.setRanged(range > 0.0D);
        channel.setRange(range);
        save();
        return null;
    }

    public String setPermission(String name, String permission) {
        ChatChannel channel = getChannel(name);
        if (channel == null) {
            return "Unknown channel: " + name;
        }
        channel.setPermission(permission);
        save();
        return null;
    }

    public String setDefaultSubscribed(String name, boolean defaultSubscribed) {
        ChatChannel channel = getChannel(name);
        if (channel == null) {
            return "Unknown channel: " + name;
        }
        channel.setDefaultSubscribed(defaultSubscribed);
        save();
        return null;
    }

    public static boolean isValidColor(String color) {
        return color != null && COLOR_PATTERN.matcher(color).matches();
    }

    private static boolean isValidChannelName(String name) {
        return name != null && !name.isBlank() && name.matches("[a-z0-9_-]+");
    }
}
