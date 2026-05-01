package com.thirdspare.modules.claims.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.Optional;

public final class ClaimsProtectionListener {
    private final ClaimsService claimsService;

    public ClaimsProtectionListener(ClaimsService claimsService) {
        this.claimsService = claimsService;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        PlayerRef actor = event.getPlayer().getPlayerRef();
        Vector3i target = event.getTargetBlock();
        if (actor == null || target == null || actor.getWorldUuid() == null) return;

        if (!claimsService.canInteract(actor, actor.getWorldUuid(), target.x, target.y, target.z)) {
            event.setCancelled(true);
            Optional<ClaimDefinition> claim = claimsService.getClaimAt(
                    actor.getWorldUuid(), target.x, target.y, target.z);
            String owner = claim.map(ClaimDefinition::getOwnerName).orElse("someone");
            actor.sendMessage(Message.raw("This area belongs to " + owner + ".").color("#FF6B6B"));
        }
    }
}
