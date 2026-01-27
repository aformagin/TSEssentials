package com.thirdspare.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.WarpData;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.StaticVariables;
import com.thirdspare.utils.Teleportation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class WarpCommand extends AbstractCommand {
    private final TSEssentials plugin;
    private final RequiredArg<String> warpNameArg;

    public WarpCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        this.plugin = plugin;
        this.warpNameArg = withRequiredArg("warp-name", "Name of the warp", ArgTypes.STRING);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        PlayerRef playerRef = CommandUtils.getPlayerFromContext(commandContext, true);
        if (playerRef == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Get required warp name from command arguments
        String warpName = commandContext.get(warpNameArg);

        // Read warp location from warp_data
        WarpData warpData = plugin.getWarpData().getWarp(warpName);

        if (warpData == null) {
            // Warp not found - send error notification
            CommandUtils.sendNotification(playerRef, "Warp Not Found!", "#FF0000",
                    "Warp '" + warpName + "' does not exist.", "#FF6B6B",
                    StaticVariables.WARP_ICON);
            return CompletableFuture.completedFuture(null);
        }

        // Teleport the player to the warp location
        Teleportation.teleportPlayer(
                playerRef,
                warpData.getX(), warpData.getY(), warpData.getZ(),
                warpData.getPitch(), warpData.getYaw(), warpData.getRoll(),
                warpData.getWorldUUID()
        );

        // Send success notification
        CommandUtils.sendNotification(playerRef, "Teleporting!", "#00FF00",
                "Warping to '" + warpName + "'!", "#228B22",
                StaticVariables.WARP_ICON);

        return CompletableFuture.completedFuture(null);
    }
}
