package com.thirdspare.modules.claims;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClaimBypassService {
    private final Set<UUID> bypassPlayers = new HashSet<>();

    public boolean toggle(UUID playerId) {
        if (bypassPlayers.remove(playerId)) {
            return false;
        }
        bypassPlayers.add(playerId);
        return true;
    }

    public boolean isBypassing(UUID playerId) {
        return bypassPlayers.contains(playerId);
    }

    public void clearAll() {
        bypassPlayers.clear();
    }
}
