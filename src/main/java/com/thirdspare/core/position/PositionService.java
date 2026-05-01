package com.thirdspare.core.position;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class PositionService {
    public String formatPosition(PlayerRef player) {
        if (player == null || player.getTransform() == null || player.getTransform().getPosition() == null) {
            return "Position is unavailable.";
        }
        Vector3d position = player.getTransform().getPosition();
        return String.format("Position: world=%s, x=%.2f, y=%.2f, z=%.2f",
                player.getWorldUuid(),
                position.getX(),
                position.getY(),
                position.getZ());
    }
}
