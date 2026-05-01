package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.TSEssentials;
import com.thirdspare.core.warps.data.WarpData;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.StaticVariables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SetWarpCommand extends AbstractCommand {
    private final TSEssentials plugin;
    private final RequiredArg<String> warpNameArg;

    public SetWarpCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        requirePermission(TSEssentialsPermissions.SET_WARP);
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

        if (warpName == null || warpName.isEmpty()) {
            CommandUtils.sendNotification(playerRef, "Invalid Warp Name!", "#FF0000",
                    "Please specify a warp name.", "#FF6B6B",
                    StaticVariables.WARP_ICON);
            return CompletableFuture.completedFuture(null);
        }

        CommandUtils.runOnPlayerWorld(commandContext, playerRef, scheduledPlayer -> {
            // Get the current location of the player
            var worldUUID = scheduledPlayer.getWorldUuid();

            // These are needed to be saved
            var playerTransformPosition = scheduledPlayer.getTransform().getPosition();
            var playerTransformRotation = scheduledPlayer.getTransform().getRotation();

            // UUID null should not be possible
            if (worldUUID == null) return;

            // Check if warp already exists
            boolean isUpdate = plugin.getWarpData().hasWarp(warpName);

            // Save current location to warp data file
            WarpData warpData = new WarpData(
                    worldUUID.toString(),
                    playerTransformPosition.getX(),
                    playerTransformPosition.getY(),
                    playerTransformPosition.getZ(),
                    playerTransformRotation.getPitch(),
                    playerTransformRotation.getYaw(),
                    playerTransformRotation.getRoll()
            );

            plugin.getWarpData().setWarp(warpName, warpData);
            plugin.saveWarpData();

            // Send the notification
            CommandUtils.sendNotification(scheduledPlayer, "Success!", "#00FF00",
                    "Warp '" + warpName + "' has been " + (isUpdate ? "updated" : "created") + ".", "#228B22",
                    StaticVariables.WARP_ICON);
        });

        return CompletableFuture.completedFuture(null);
    }
}
