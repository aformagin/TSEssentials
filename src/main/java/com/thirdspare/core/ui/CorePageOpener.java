package com.thirdspare.core.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

/**
 * Centralizes opening core custom UI pages on the player's owning world thread.
 * Hytale page operations need the player ECS reference, store, and Player component,
 * so commands use this helper instead of duplicating that thread/context lookup.
 */
public final class CorePageOpener {
    private CorePageOpener() {
    }

    public static void open(PlayerRef player, CustomUIPage page) {
        if (player == null || page == null || player.getWorldUuid() == null) {
            return;
        }
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                var ref = player.getReference();
                var store = ref.getStore();
                Player playerComponent = store.getComponent(ref, Player.getComponentType());
                if (playerComponent != null) {
                    playerComponent.getPageManager().openCustomPage(ref, store, page);
                }
            } catch (RuntimeException ignored) {
            }
        });
    }
}
