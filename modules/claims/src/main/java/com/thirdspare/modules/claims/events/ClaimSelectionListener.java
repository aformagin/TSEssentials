package com.thirdspare.modules.claims.events;

import com.thirdspare.modules.claims.ClaimSelectionService;

/**
 * Placeholder for future selection-tool-based corner setting.
 * v1 uses /claim pos1 and /claim pos2 commands instead.
 */
public final class ClaimSelectionListener {
    private final ClaimSelectionService selectionService;

    public ClaimSelectionListener(ClaimSelectionService selectionService) {
        this.selectionService = selectionService;
    }
}
