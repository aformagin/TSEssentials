package com.thirdspare.events.claims;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.claims.ClaimManager;
import com.thirdspare.data.claims.Claim;
import org.joml.Vector3i;

public class ClaimProtectionListener {
    private final ClaimManager claimManager;

    public ClaimProtectionListener(ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        PlayerRef actor = event.getPlayer().getPlayerRef();
        Vector3i target = event.getTargetBlock();
        if (actor == null || target == null || actor.getWorldUuid() == null) {
            return;
        }

        Claim claim = claimManager.findClaimAt(actor.getWorldUuid().toString(), target.x, target.y, target.z);
        if (claimManager.canAccess(actor, claim)) {
            return;
        }

        event.setCancelled(true);
        actor.sendMessage(Message.raw("This area belongs to " + claim.getOwnerName() + ".").color("#FF6B6B"));
    }
}
