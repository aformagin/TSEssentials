package com.thirdspare.utils;

import com.hypixel.hytale.builtin.beds.BedsPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Utility class for player teleportation operations
 */
public class Teleportation {

    /**
     * Teleport a player to a specific location with rotation
     *
     * @param playerRef The player reference to teleport
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param z         Z coordinate
     * @param pitch     Pitch rotation (looking up/down)
     * @param yaw       Yaw rotation (turning left/right)
     * @param roll      Roll rotation (tilting sideways)
     * @param worldUUID The UUID of the world to teleport to
     */
    public static void teleportPlayer(PlayerRef playerRef, double x, double y, double z,
                                     float pitch, float yaw, float roll,
                                     UUID worldUUID) {
        World world = Universe.get().getWorld(worldUUID);

        if (world == null) {
            // World doesn't exist - cannot teleport
            return;
        }

        world.execute(() -> {
            if (playerRef.getReference() == null) return;
            Store<EntityStore> store = playerRef.getReference().getStore();

            // Create position and rotation vectors
            Vector3d position = new Vector3d(x, y, z);
            Vector3f rotation = new Vector3f(pitch, yaw, roll);

            // Create and apply teleport component
            Teleport teleport = new Teleport(position, rotation);
            store.addComponent(playerRef.getReference(), Teleport.getComponentType(), teleport);
        });
    }

    /**
     * Teleport a player to a specific location without changing rotation
     *
     * @param playerRef The player reference to teleport
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param z         Z coordinate
     * @param worldUUID The UUID of the world to teleport to
     */
    public static void teleportPlayer(PlayerRef playerRef, double x, double y, double z, UUID worldUUID) {
        World world = Universe.get().getWorld(worldUUID);
        if (world == null) {
            return;
        }

        world.execute(() -> {
            if (playerRef.getReference() == null) return;
            Store<EntityStore> store = playerRef.getReference().getStore();

            // Get current rotation to preserve it
            Vector3f currentRotation = playerRef.getTransform().getRotation();

            // Create position vector
            Vector3d position = new Vector3d(x, y, z);

            // Create and apply teleport component
            Teleport teleport = new Teleport(position, currentRotation);
            store.addComponent(playerRef.getReference(), Teleport.getComponentType(), teleport);
        });
    }

    /**
     * Teleport a player using a Transform object
     *
     * @param playerRef The player reference to teleport
     * @param transform The target transform (position and rotation)
     * @param worldUUID The UUID of the world to teleport to
     */
    public static void teleportPlayer(PlayerRef playerRef, Transform transform, UUID worldUUID) {
        World world = Universe.get().getWorld(worldUUID);
        if (world == null) {
            return;
        }

        world.execute(() -> {
            if (playerRef.getReference() == null) return;
            Store<EntityStore> store = playerRef.getReference().getStore();

            // Create and apply teleport component using transform's position and rotation
            Teleport teleport = new Teleport(transform.getPosition(), transform.getRotation());
            store.addComponent(playerRef.getReference(), Teleport.getComponentType(), teleport);
        });
    }
}