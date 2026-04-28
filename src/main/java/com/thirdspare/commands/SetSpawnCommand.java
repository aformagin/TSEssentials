package com.thirdspare.commands;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.SpawnData;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.StaticVariables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SetSpawnCommand extends AbstractCommand {
    private final TSEssentials plugin;

    public SetSpawnCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        requirePermission(TSEssentialsPermissions.SET_SPAWN);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        PlayerRef playerRef = CommandUtils.getPlayerFromContext(commandContext, true);
        if (playerRef == null) {
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
                playerTransformPosition.x(),
                playerTransformPosition.y(),
                playerTransformPosition.z(),
                playerTransformRotation.pitch(),
                playerTransformRotation.yaw(),
                playerTransformRotation.roll()
        );

        plugin.getSpawnData().setSpawn(spawnData);
        plugin.saveSpawnData();

        // Update the world's native spawn provider so new players, death respawns,
        // and the map marker all use this location.
        // Must create new vector instances - Transform stores references, not copies.
        World world = Universe.get().getWorld(worldUUID);
        if (world != null) {
            org.joml.Vector3d spawnPosition = new org.joml.Vector3d(
                    playerTransformPosition.x(),
                    playerTransformPosition.y(),
                    playerTransformPosition.z()
            );
            Rotation3f spawnRotation = new Rotation3f(0, playerTransformRotation.yaw(), 0);
            Transform spawnTransform = new Transform(spawnPosition, spawnRotation);
            world.getWorldConfig().setSpawnProvider(new GlobalSpawnProvider(spawnTransform));
        }

        // Send the notification
        CommandUtils.sendNotification(playerRef, "Success!", "#00FF00",
                "Server spawn has been " + (isUpdate ? "updated" : "set") + "!", "#228B22",
                StaticVariables.SPAWN_ICON);

        return CompletableFuture.completedFuture(null);
    }
}
