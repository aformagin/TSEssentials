package com.thirdspare.core.back;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.thirdspare.utils.Teleportation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackService {
    private final Map<UUID, PreviousLocation> previousLocations = new ConcurrentHashMap<>();

    public void recordBeforeTeleport(PlayerRef player, String reason) {
        if (player == null || !player.isValid() || player.getWorldUuid() == null || player.getTransform() == null) {
            return;
        }
        Vector3d position = new Vector3d(player.getTransform().getPosition());
        Vector3f rotation = new Vector3f(player.getTransform().getRotation());
        previousLocations.put(player.getUuid(), new PreviousLocation(
                player.getWorldUuid(),
                position,
                rotation,
                Instant.now(),
                reason != null ? reason : "teleport"
        ));
    }

    public PreviousLocation getPreviousLocation(PlayerRef player) {
        return player != null ? previousLocations.get(player.getUuid()) : null;
    }

    public BackResult teleportBack(PlayerRef player) {
        PreviousLocation location = getPreviousLocation(player);
        if (player == null) {
            return new BackResult(false, "Player is unavailable.");
        }
        if (location == null) {
            return new BackResult(false, "No previous location has been recorded.");
        }
        if (Universe.get().getWorld(location.worldUuid()) == null) {
            return new BackResult(false, "The previous world is no longer available.");
        }

        Teleportation.teleportPlayerWithoutBackCapture(player, location.position(), location.rotation(), location.worldUuid());
        return new BackResult(true, "Returned to your previous location.");
    }

    public record BackResult(boolean success, String message) {
    }
}
