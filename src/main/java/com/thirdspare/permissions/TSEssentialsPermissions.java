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
    public static final String BALANCE = "tsessentials.command.balance";
    public static final String BALANCE_OTHERS = "tsessentials.command.balance.others";
    public static final String PAY = "tsessentials.command.pay";
    public static final String ECONOMY_UI = "tsessentials.command.economy";
    public static final String ECO = "tsessentials.command.eco";
    public static final String ECO_ADMIN_UI = "tsessentials.command.ecoadmin";
    public static final String ECO_GIVE = "tsessentials.command.eco.give";
    public static final String ECO_TAKE = "tsessentials.command.eco.take";
    public static final String ECO_SET = "tsessentials.command.eco.set";

    private TSEssentialsPermissions() {
    }

    public static String shortcut(String command) {
        return "tsessentials.command." + command;
    }
}
