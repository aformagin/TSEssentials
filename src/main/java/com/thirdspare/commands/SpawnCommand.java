package com.thirdspare.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.SpawnData;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.StaticVariables;
import com.thirdspare.utils.Teleportation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SpawnCommand extends AbstractCommand {
    private final TSEssentials plugin;

    public SpawnCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        requirePermission(TSEssentialsPermissions.SPAWN);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        PlayerRef playerRef = CommandUtils.getPlayerFromContext(commandContext, true);
        if (playerRef == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Get spawn location
        SpawnData spawnData = plugin.getSpawnData().getSpawn();

        if (spawnData == null) {
            // Spawn not set - send error notification
            CommandUtils.sendNotification(playerRef, "Spawn Not Set!", "#FF0000",
                    "No server spawn has been configured.", "#FF6B6B",
                    StaticVariables.SPAWN_ICON);
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
        CommandUtils.sendNotification(playerRef, "Teleporting!", "#00FF00",
                "Warping to spawn!", "#228B22",
                StaticVariables.SPAWN_ICON);

        return CompletableFuture.completedFuture(null);
    }
}
