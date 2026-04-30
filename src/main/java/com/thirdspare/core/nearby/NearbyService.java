package com.thirdspare.core.nearby;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.ArrayList;
import java.util.List;

public class NearbyService {
    public static final double DEFAULT_RANGE = 150.0D;

    public List<NearbyPlayer> findNearby(PlayerRef player) {
        List<NearbyPlayer> result = new ArrayList<>();
        if (player == null || player.getTransform() == null) {
            return result;
        }
        Vector3d origin = player.getTransform().getPosition();
        for (PlayerRef other : Universe.get().getPlayers()) {
            if (other == null || !other.isValid() || other.getUuid().equals(player.getUuid())) {
                continue;
            }
            if (!player.getWorldUuid().equals(other.getWorldUuid()) || other.getTransform() == null) {
                continue;
            }
            double distance = distance(origin, other.getTransform().getPosition());
            if (distance <= DEFAULT_RANGE) {
                result.add(new NearbyPlayer(other.getUsername(), distance));
            }
        }
        result.sort((left, right) -> Double.compare(left.distance(), right.distance()));
        return result;
    }

    private double distance(Vector3d left, Vector3d right) {
        double dx = left.getX() - right.getX();
        double dy = left.getY() - right.getY();
        double dz = left.getZ() - right.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public record NearbyPlayer(String username, double distance) {
    }
}
