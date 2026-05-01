package com.thirdspare.commands.core;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.thirdspare.TSEssentials;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.core.tpa.TeleportRequest;
import com.thirdspare.core.tpa.TeleportRequestType;
import com.thirdspare.utils.PlayerLookup;
import com.thirdspare.utils.StaticVariables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TpaCommand extends AbstractCommand {
    private final TSEssentials plugin;
    private final RequiredArg<String> usernameArg;

    public TpaCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        requirePermission(TSEssentialsPermissions.TPA);
        this.plugin = plugin;
        this.usernameArg = withRequiredArg("username", "Player to send teleport request to", ArgTypes.STRING);
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

        String targetUsername = commandContext.get(usernameArg);
        Optional<PlayerRef> targetOpt = PlayerLookup.findPlayerByName(targetUsername);

        if (targetOpt.isEmpty()) {
            sendNotification(playerRef, "Player Not Found", "Could not find player '" + targetUsername + "'", "#FF0000", "#FF6B6B");
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef targetRef = targetOpt.get();

        if (targetRef.getUuid().equals(playerUUID)) {
            sendNotification(playerRef, "Invalid Target", "You cannot send a teleport request to yourself!", "#FF0000", "#FF6B6B");
            return CompletableFuture.completedFuture(null);
        }

        // Create and add the request
        TeleportRequest request = new TeleportRequest(
                playerUUID,
                targetRef.getUuid(),
                TeleportRequestType.TPA,
                playerRef.getUsername()
        );
        plugin.getTeleportRequestManager().addRequest(request);

        // Notify requester
        sendNotification(playerRef, "Request Sent", "Teleport request sent to " + targetRef.getUsername(), "#00FF00", "#228B22");

        // Notify target
        sendNotification(targetRef, "Teleport Request",
                playerRef.getUsername() + " wants to teleport to you. Use /tpaccept or /tpdeny",
                "#FFD700", "#FFA500");

        return CompletableFuture.completedFuture(null);
    }

    private void sendNotification(PlayerRef playerRef, String primary, String secondary, String primaryColor, String secondaryColor) {
        var packetHandler = playerRef.getPacketHandler();
        if (packetHandler != null) {
            var primaryMessage = Message.raw(primary).color(primaryColor);
            var secondaryMessage = Message.raw(secondary).color(secondaryColor);
            var icon = new ItemStack(StaticVariables.TPA_ICON, 1).toPacket();

            NotificationUtil.sendNotification(
                    packetHandler,
                    primaryMessage,
                    secondaryMessage,
                    (ItemWithAllMetadata) icon);
        }
    }
}
