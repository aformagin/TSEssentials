package com.thirdspare.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.WarpData;
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

        // Get the current location of the player
        var worldUUID = playerRef.getWorldUuid();

        // These are needed to be saved
        var playerTransformPosition = playerRef.getTransform().getPosition();
        var playerTransformRotation = playerRef.getTransform().getRotation();

        // UUID null should not be possible
        if (worldUUID == null) return CompletableFuture.completedFuture(null);

        // Check if warp already exists
        boolean isUpdate = plugin.getWarpData().hasWarp(warpName);

        // Save current location to warp data file
        WarpData warpData = new WarpData(
                worldUUID.toString(),
                playerTransformPosition.x(),
                playerTransformPosition.y(),
                playerTransformPosition.z(),
                playerTransformRotation.pitch(),
                playerTransformRotation.yaw(),
                playerTransformRotation.roll()
        );

        plugin.getWarpData().setWarp(warpName, warpData);
        plugin.saveWarpData();

        // Send the notification
        CommandUtils.sendNotification(playerRef, "Success!", "#00FF00",
                "Warp '" + warpName + "' has been " + (isUpdate ? "updated" : "created") + ".", "#228B22",
                StaticVariables.WARP_ICON);

        return CompletableFuture.completedFuture(null);
    }
}
