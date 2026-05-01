package com.thirdspare.modules.claims;

import com.thirdspare.modules.claims.data.ClaimBounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClaimSelectionService {

    public record Corner(UUID worldUuid, int x, int y, int z) {
    }

    private static final class SelectionState {
        Corner pos1;
        Corner pos2;
    }

    private final Map<UUID, SelectionState> selections = new HashMap<>();

    public void setPos1(UUID playerId, UUID worldUuid, int x, int y, int z) {
        state(playerId).pos1 = new Corner(worldUuid, x, y, z);
    }

    public void setPos2(UUID playerId, UUID worldUuid, int x, int y, int z) {
        state(playerId).pos2 = new Corner(worldUuid, x, y, z);
    }

    public void clear(UUID playerId) {
        selections.remove(playerId);
    }

    public Corner getPos1(UUID playerId) {
        SelectionState s = selections.get(playerId);
        return s == null ? null : s.pos1;
    }

    public Corner getPos2(UUID playerId) {
        SelectionState s = selections.get(playerId);
        return s == null ? null : s.pos2;
    }

    public String validateSelection(UUID playerId) {
        SelectionState s = selections.get(playerId);
        if (s == null || s.pos1 == null || s.pos2 == null) {
            return "Set both corners first with /claim pos1 and /claim pos2.";
        }
        if (!s.pos1.worldUuid().equals(s.pos2.worldUuid())) {
            return "Both corners must be in the same world.";
        }
        ClaimBounds bounds = buildBounds(s);
        if (bounds.isZeroVolume()) {
            return "Claims must have width, height, and depth.";
        }
        return null;
    }

    public ClaimBounds buildBoundsFor(UUID playerId) {
        SelectionState s = selections.get(playerId);
        if (s == null || s.pos1 == null || s.pos2 == null) return null;
        return buildBounds(s);
    }

    public UUID getSelectionWorldUuid(UUID playerId) {
        SelectionState s = selections.get(playerId);
        return s == null || s.pos1 == null ? null : s.pos1.worldUuid();
    }

    public void clearAll() {
        selections.clear();
    }

    private SelectionState state(UUID playerId) {
        return selections.computeIfAbsent(playerId, id -> new SelectionState());
    }

    private static ClaimBounds buildBounds(SelectionState s) {
        return new ClaimBounds(
                s.pos1.x(), s.pos1.y(), s.pos1.z(),
                s.pos2.x(), s.pos2.y(), s.pos2.z()
        );
    }
}
