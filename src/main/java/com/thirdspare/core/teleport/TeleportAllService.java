package com.thirdspare.core.teleport;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.thirdspare.utils.Teleportation;

public class TeleportAllService {
    public int teleportAllTo(PlayerRef executor) {
        if (executor == null || executor.getTransform() == null || executor.getWorldUuid() == null) {
            return 0;
        }
        int teleported = 0;
        for (PlayerRef target : Universe.get().getPlayers()) {
            if (target == null || !target.isValid() || target.getUuid().equals(executor.getUuid())) {
                continue;
            }
            Teleportation.teleportPlayer(target, executor.getTransform(), executor.getWorldUuid(), "tpall");
            teleported++;
        }
        return teleported;
    }
}
