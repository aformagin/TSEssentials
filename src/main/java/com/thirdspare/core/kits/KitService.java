package com.thirdspare.core.kits;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.kits.data.KitDefinition;
import com.thirdspare.utils.CommandUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitService {
    private final KitManager manager;
    private final Map<UUID, Map<String, Instant>> cooldowns = new HashMap<>();

    public KitService(KitManager manager) {
        this.manager = manager;
    }

    public KitManager getManager() {
        return manager;
    }

    public List<KitDefinition> accessibleKits(PlayerRef player) {
        List<KitDefinition> result = new ArrayList<>();
        for (KitDefinition kit : manager.getKits()) {
            if (kit.isEnabled() && hasAccess(player, kit)) {
                result.add(kit);
            }
        }
        return result;
    }

    public GrantResult grant(PlayerRef player, String kitName) {
        KitDefinition kit = manager.getKit(kitName);
        if (kit == null) {
            return GrantResult.error("Unknown kit: " + kitName);
        }
        if (!kit.isEnabled()) {
            return GrantResult.error("That kit is disabled.");
        }
        if (!hasAccess(player, kit)) {
            return GrantResult.error("You do not have access to that kit.");
        }
        long remaining = remainingCooldown(player, kit);
        if (remaining > 0L) {
            return GrantResult.error("That kit is on cooldown for " + remaining + " more second(s).");
        }

        Player playerComponent = player.getComponent(Player.getComponentType());
        if (playerComponent == null) {
            return GrantResult.error("Unable to access your inventory.");
        }
        List<ItemStack> items = manager.toItemStacks(kit);
        if (items.isEmpty()) {
            return GrantResult.error("That kit has no items configured.");
        }

        ItemContainer target = playerComponent.getInventory().getCombinedStorageFirst();
        if (!target.canAddItemStacks(items)) {
            return GrantResult.error("You do not have enough inventory space for that kit.");
        }
        if (!target.addItemStacks(items).succeeded()) {
            return GrantResult.error("Unable to grant that kit.");
        }
        markCooldown(player, kit);
        return new GrantResult(true, "Granted kit " + kit.getDisplayName() + ".");
    }

    private boolean hasAccess(PlayerRef player, KitDefinition kit) {
        String permission = kit.getPermission();
        return permission == null || permission.isBlank() || CommandUtils.hasPermission(player, permission);
    }

    private long remainingCooldown(PlayerRef player, KitDefinition kit) {
        if (kit.getCooldownSeconds() <= 0L) {
            return 0L;
        }
        Instant last = cooldowns.getOrDefault(player.getUuid(), Map.of()).get(kit.getName());
        if (last == null) {
            return 0L;
        }
        long elapsed = Duration.between(last, Instant.now()).toSeconds();
        return Math.max(0L, kit.getCooldownSeconds() - elapsed);
    }

    private void markCooldown(PlayerRef player, KitDefinition kit) {
        if (kit.getCooldownSeconds() > 0L) {
            cooldowns.computeIfAbsent(player.getUuid(), ignored -> new HashMap<>()).put(kit.getName(), Instant.now());
        }
    }

    public record GrantResult(boolean success, String message) {
        static GrantResult error(String message) {
            return new GrantResult(false, message);
        }
    }
}
