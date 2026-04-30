package com.thirdspare.modules.chat;

import com.thirdspare.modules.core.PermissionNodeDescriptor;

import java.util.List;

public final class TSEChatPermissionsNodes {
    public static final String CHANNEL = "tsessentials.command.channel";
    public static final String CHANNEL_LIST = "tsessentials.command.channel.list";
    public static final String CHANNEL_FOCUS = "tsessentials.command.channel.focus";
    public static final String CHANNEL_JOIN = "tsessentials.command.channel.join";
    public static final String CHANNEL_LEAVE = "tsessentials.command.channel.leave";
    public static final String GLOBAL_CHAT = "tsessentials.command.g";
    public static final String LOCAL_CHAT = "tsessentials.command.l";
    public static final String STAFF_CHAT = "tsessentials.command.sc";
    public static final String CHAT_STAFF = "tsessentials.chat.staff";
    public static final String IGNORE = "tsessentials.command.ignore";
    public static final String UNIGNORE = "tsessentials.command.unignore";
    public static final String NICK = "tsessentials.command.nick";
    public static final String NICK_COLOR = "tsessentials.command.nickcolor";
    public static final String CHAT_EDIT = "tsessentials.command.chatedit";

    private TSEChatPermissionsNodes() {
    }

    public static String shortcut(String command) {
        return switch (command) {
            case "g" -> GLOBAL_CHAT;
            case "l" -> LOCAL_CHAT;
            case "sc" -> STAFF_CHAT;
            default -> "tsessentials.command." + command;
        };
    }

    public static List<PermissionNodeDescriptor> permissionNodes() {
        return List.of(
                new PermissionNodeDescriptor(CHANNEL, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(CHANNEL_LIST, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(CHANNEL_FOCUS, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(CHANNEL_JOIN, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(CHANNEL_LEAVE, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(GLOBAL_CHAT, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(LOCAL_CHAT, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(STAFF_CHAT, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(CHAT_STAFF, "TSEssentials Chat", "Chat channel permission"),
                new PermissionNodeDescriptor(IGNORE, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(UNIGNORE, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(NICK, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(NICK_COLOR, "TSEssentials Chat", "Chat module command"),
                new PermissionNodeDescriptor(CHAT_EDIT, "TSEssentials Chat", "Chat module command")
        );
    }
}
