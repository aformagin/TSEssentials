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
import com.thirdspare.utils.Teleportation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SpawnCommand extends AbstractCommand {
    private final TSEssentials plugin;

    public SpawnCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
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

        // Get spawn location
        SpawnData spawnData = plugin.getSpawnData().getSpawn();

        if (spawnData == null) {
            // Spawn not set - send error notification
            var packetHandler = playerRef.getPacketHandler();
            if (packetHandler != null) {
                var primaryMessage = Message.raw("Spawn Not Set!").color("#FF0000");
                var secondaryMessage = Message.raw("No server spawn has been configured.").color("#FF6B6B");
                var icon = new ItemStack(StaticVariables.SPAWN_ICON, 1).toPacket();

                NotificationUtil.sendNotification(
                        packetHandler,
                        primaryMessage,
                        secondaryMessage,
                        (ItemWithAllMetadata) icon);
            }
            return CompletableFuture.completedFuture(null);
        }

        // Teleport the player to spawn
        Teleportation.teleportPlayer(
                playerRef,
                spawnData.getX(), spawnData.getY(), spawnData.getZ(),
                spawnData.getPitch(), spawnData.getYaw(), spawnData.getRoll(),
                spawnData.getWorldUUID()
        );

        // Send success notification
        var packetHandler = playerRef.getPacketHandler();
        if (packetHandler != null) {
            var primaryMessage = Message.raw("Teleporting!").color("#00FF00");
            var secondaryMessage = Message.raw("Warping to spawn!").color("#228B22");
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