package com.thirdspare.permissions;

public final class TSEssentialsPermissions {
    public static final String COMMAND_WILDCARD = "tsessentials.command.*";

    public static final String HOME = "tsessentials.command.home";
    public static final String SET_HOME = "tsessentials.command.sethome";
    public static final String WARP = "tsessentials.command.warp";
    public static final String SET_WARP = "tsessentials.command.setwarp";
    public static final String SPAWN = "tsessentials.command.spawn";
    public static final String SET_SPAWN = "tsessentials.command.setspawn";
    public static final String TPA = "tsessentials.command.tpa";
    public static final String TPA_HERE = "tsessentials.command.tpahere";
    public static final String TP_ACCEPT = "tsessentials.command.tpaccept";
    public static final String TP_DENY = "tsessentials.command.tpdeny";
    public static final String TP_HERE = "tsessentials.command.tphere";
    public static final String CHANNEL = "tsessentials.command.channel";
    public static final String CHANNEL_LIST = "tsessentials.command.channel.list";
    public static final String CHANNEL_FOCUS = "tsessentials.command.channel.focus";
    public static final String CHANNEL_JOIN = "tsessentials.command.channel.join";
    public static final String CHANNEL_LEAVE = "tsessentials.command.channel.leave";
    public static final String GLOBAL_CHAT = "tsessentials.command.g";
    public static final String LOCAL_CHAT = "tsessentials.command.l";
    public static final String STAFF_CHAT = "tsessentials.command.sc";
    public static final String IGNORE = "tsessentials.command.ignore";
    public static final String UNIGNORE = "tsessentials.command.unignore";
    public static final String NICK = "tsessentials.command.nick";
    public static final String NICK_COLOR = "tsessentials.command.nickcolor";
    public static final String CHAT_EDIT = "tsessentials.command.chatedit";

    private TSEssentialsPermissions() {
    }

    public static String shortcut(String command) {
        return switch (command) {
            case "g" -> GLOBAL_CHAT;
            case "l" -> LOCAL_CHAT;
            case "sc" -> STAFF_CHAT;
            default -> "tsessentials.command." + command;
        };
    }
}
