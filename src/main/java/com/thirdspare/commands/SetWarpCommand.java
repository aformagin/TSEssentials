package com.thirdspare.commands;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.WarpData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SetWarpCommand extends AbstractCommand {
    private final TSEssentials plugin;
    private final RequiredArg<String> warpNameArg;

    public SetWarpCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        this.plugin = plugin;
        this.warpNameArg = withRequiredArg("warp-name", "Name of the warp", ArgTypes.STRING);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        if (!commandContext.isPlayer()) {
            commandContext.sendMessage(Message.raw("This command can only be used by players!").color("#FF0000"));
            return CompletableFuture.completedFuture(null);
        }

        var playerUUID = commandContext.sender().getUuid();
        var playerRef = Universe.get().getPlayer(playerUUID);

        if (playerRef == null) {
            commandContext.sendMessage(Message.raw("Unable to find player!").color("#FF0000"));
            return CompletableFuture.completedFuture(null);
        }

        // Get required warp name from command arguments
        String warpName = commandContext.get(warpNameArg);

        if (warpName == null || warpName.isEmpty()) {
            commandContext.sendMessage(Message.raw("Please specify a warp name!").color("#FF0000"));
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
        var packetHandler = playerRef.getPacketHandler();
        if (packetHandler != null) {
            var primaryMessage = Message.raw("Success!").color("#00FF00");
            var secondaryMessage = Message.raw("Warp '" + warpName + "' has been " +
                    (isUpdate ? "updated" : "created") + ".").color("#228B22");
            var icon = new ItemStack("Furniture_Village_Brazier", 1).toPacket();

            NotificationUtil.sendNotification(
                    packetHandler,
                    primaryMessage,
                    secondaryMessage,
                    (ItemWithAllMetadata) icon);
        }

        return CompletableFuture.completedFuture(null);
    }
}
