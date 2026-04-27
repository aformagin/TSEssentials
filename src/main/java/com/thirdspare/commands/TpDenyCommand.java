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
import com.thirdspare.tpa.TeleportRequest;
import com.thirdspare.utils.StaticVariables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class TpDenyCommand extends AbstractCommand {
    private final TSEssentials plugin;

    public TpDenyCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
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

        // Get requester for notification (may be offline)
        PlayerRef requesterRef = Universe.get().getPlayer(request.getRequesterUUID());

        // Remove the request
        plugin.getTeleportRequestManager().removeRequest(request);

        // Notify the player who denied
        sendNotification(playerRef, "Request Denied", "Teleport request denied", "#FF0000", "#FF6B6B");

        // Notify the requester if they're still online
        if (requesterRef != null) {
            sendNotification(requesterRef, "Request Denied", playerRef.getUsername() + " denied your teleport request", "#FF0000", "#FF6B6B");
        }

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
