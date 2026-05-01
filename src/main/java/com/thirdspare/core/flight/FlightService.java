package com.thirdspare.core.flight;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightService {
    private static final float DEFAULT_FLY_SPEED = 10.32F;

    private final Map<UUID, FlightSnapshot> enabledFlight = new HashMap<>();

    public FlightResult toggle(PlayerRef player) {
        if (player == null) {
            return new FlightResult(false, false, "Player is unavailable.");
        }

        Ref<EntityStore> playerRef = player.getReference();
        if (playerRef == null || !playerRef.isValid()) {
            enabledFlight.remove(player.getUuid());
            return new FlightResult(false, false, "Player is not currently in a world.");
        }

        Store<EntityStore> store = playerRef.getStore();
        if (store == null) {
            return new FlightResult(false, false, "Unable to access player world data.");
        }

        MovementManager movementManager = store.getComponent(playerRef, MovementManager.getComponentType());
        if (movementManager == null) {
            return new FlightResult(false, false, "Player movement settings are unavailable.");
        }

        PacketHandler packetHandler = player.getPacketHandler();
        if (packetHandler == null) {
            return new FlightResult(false, false, "Player connection is unavailable.");
        }

        UUID playerUuid = player.getUuid();
        FlightSnapshot snapshot = enabledFlight.remove(playerUuid);
        if (snapshot != null) {
            restore(movementManager.getDefaultSettings(), snapshot.defaultCanFly());
            restore(movementManager.getSettings(), snapshot.liveCanFly());
            movementManager.update(packetHandler);
            return new FlightResult(true, false, "Flight disabled.");
        }

        MovementSettings defaultSettings = movementManager.getDefaultSettings();
        MovementSettings liveSettings = movementManager.getSettings();
        boolean defaultCanFly = defaultSettings != null && defaultSettings.canFly;
        boolean liveCanFly = liveSettings != null && liveSettings.canFly;
        if (defaultCanFly || liveCanFly) {
            return new FlightResult(true, true, "Flight is already enabled.");
        }

        enable(defaultSettings);
        enable(liveSettings);
        enabledFlight.put(playerUuid, new FlightSnapshot(defaultCanFly, liveCanFly));
        movementManager.update(packetHandler);
        return new FlightResult(true, true, "Flight enabled.");
    }

    private void enable(MovementSettings settings) {
        if (settings == null) {
            return;
        }
        settings.canFly = true;
        if (settings.horizontalFlySpeed <= 0.0F) {
            settings.horizontalFlySpeed = DEFAULT_FLY_SPEED;
        }
        if (settings.verticalFlySpeed <= 0.0F) {
            settings.verticalFlySpeed = DEFAULT_FLY_SPEED;
        }
    }

    private void restore(MovementSettings settings, boolean canFly) {
        if (settings != null) {
            settings.canFly = canFly;
        }
    }

    private record FlightSnapshot(boolean defaultCanFly, boolean liveCanFly) {
    }

    public record FlightResult(boolean supported, boolean requestedEnabled, String message) {
    }
}
