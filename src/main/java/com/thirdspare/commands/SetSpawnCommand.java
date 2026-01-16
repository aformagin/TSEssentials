package com.thirdspare.commands;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.SpawnData;
import com.thirdspare.utils.StaticVariables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SetSpawnCommand extends AbstractCommand {
    private final TSEssentials plugin;

    public SetSpawnCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
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

        // Get the current location of the player
        var worldUUID = playerRef.getWorldUuid();

        // These are needed to be saved
        var playerTransformPosition = playerRef.getTransform().getPosition();
        var playerTransformRotation = playerRef.getTransform().getRotation();

        // UUID null should not be possible
        if (worldUUID == null) return CompletableFuture.completedFuture(null);

        // Check if spawn already exists
        boolean isUpdate = plugin.getSpawnData().hasSpawn();

        // Save current location as spawn
        SpawnData spawnData = new SpawnData(
                worldUUID.toString(),
                playerTransformPosition.getX(),
                playerTransformPosition.getY(),
                playerTransformPosition.getZ(),
                playerTransformRotation.getPitch(),
                playerTransformRotation.getYaw(),
                playerTransformRotation.getRoll()
        );

        plugin.getSpawnData().setSpawn(spawnData);
        plugin.saveSpawnData();

        // Send the notification
        var packetHandler = playerRef.getPacketHandler();
        if (packetHandler != null) {
            var primaryMessage = Message.raw("Success!").color("#00FF00");
            var secondaryMessage = Message.raw("Server spawn has been " +
                    (isUpdate ? "updated" : "set") + "!").color("#228B22");
            var icon = new ItemStack(StaticVariables.SPAWN_ICON, 1).toPacket();

            NotificationUtil.sendNotification(
                    packetHandler,
                    primaryMessage,
                    secondaryMessage,
                    (ItemWithAllMetadata) icon);
        }

        return CompletableFuture.completedFuture(null);
    }
}