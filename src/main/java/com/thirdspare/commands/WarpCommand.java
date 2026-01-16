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

        // Read warp location from warp_data
        WarpData warpData = plugin.getWarpData().getWarp(warpName);

        if (warpData == null) {
            // Warp not found - send error notification
            var packetHandler = playerRef.getPacketHandler();
            if (packetHandler != null) {
                var primaryMessage = Message.raw("Warp Not Found!").color("#FF0000");
                var secondaryMessage = Message.raw("Warp '" + warpName + "' does not exist.").color("#FF6B6B");
                var icon = new ItemStack(StaticVariables.WARP_ICON, 1).toPacket();

                NotificationUtil.sendNotification(
                        packetHandler,
                        primaryMessage,
                        secondaryMessage,
                        (ItemWithAllMetadata) icon);
            }
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
        var packetHandler = playerRef.getPacketHandler();
        if (packetHandler != null) {
            var primaryMessage = Message.raw("Teleporting!").color("#00FF00");
            var secondaryMessage = Message.raw("Warping to '" + warpName + "'!").color("#228B22");
            var icon = new ItemStack(StaticVariables.WARP_ICON, 1).toPacket();

            NotificationUtil.sendNotification(
                    packetHandler,
                    primaryMessage,
                    secondaryMessage,
                    (ItemWithAllMetadata) icon);
        }

        return CompletableFuture.completedFuture(null);
    }
}
