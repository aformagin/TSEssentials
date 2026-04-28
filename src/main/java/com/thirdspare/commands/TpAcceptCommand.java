package com.thirdspare.commands;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.thirdspare.TSEssentials;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.tpa.TeleportRequest;
import com.thirdspare.tpa.TeleportRequestType;
import com.thirdspare.utils.StaticVariables;
import com.thirdspare.utils.Teleportation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class TpAcceptCommand extends AbstractCommand {
    private final TSEssentials plugin;

    public TpAcceptCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        requirePermission(TSEssentialsPermissions.TP_ACCEPT);
        this.plugin = plugin;
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

        // Get the most recent request for this player
        TeleportRequest request = plugin.getTeleportRequestManager().getMostRecentRequest(playerUUID);

        if (request == null) {
            sendNotification(playerRef, "No Request", "You have no pending teleport requests", "#FF0000", "#FF6B6B");
            return CompletableFuture.completedFuture(null);
        }

        if (request.isExpired()) {
            plugin.getTeleportRequestManager().removeRequest(request);
            sendNotification(playerRef, "Request Expired", "The teleport request has expired", "#FF0000", "#FF6B6B");
            return CompletableFuture.completedFuture(null);
        }

        // Get requester
        PlayerRef requesterRef = Universe.get().getPlayer(request.getRequesterUUID());
        if (requesterRef == null) {
            plugin.getTeleportRequestManager().removeRequest(request);
            sendNotification(playerRef, "Player Offline", "The requesting player is no longer online", "#FF0000", "#FF6B6B");
            return CompletableFuture.completedFuture(null);
        }

        // Determine who teleports to whom based on request type
        PlayerRef teleportingPlayer;
        PlayerRef destinationPlayer;

        if (request.getType() == TeleportRequestType.TPA) {
            // Requester teleports to accepter (target)
            teleportingPlayer = requesterRef;
            destinationPlayer = playerRef;
        } else {
            // TPAHERE: Accepter (target) teleports to requester
            teleportingPlayer = playerRef;
            destinationPlayer = requesterRef;
        }

        // Get destination player's position
        var destTransform = destinationPlayer.getTransform();

        var destWorldUUID = destinationPlayer.getWorldUuid();

        // Execute teleport
        Teleportation.teleportPlayer(
                teleportingPlayer,
                destTransform.getPosition().x,
                destTransform.getPosition().y,
                destTransform.getPosition().z,
                destWorldUUID
        );

        // Remove the request
        plugin.getTeleportRequestManager().removeRequest(request);

        // Notify both players
        sendNotification(playerRef, "Request Accepted", "Teleport request accepted!", "#00FF00", "#228B22");
        sendNotification(requesterRef, "Request Accepted", playerRef.getUsername() + " accepted your teleport request!", "#00FF00", "#228B22");

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
