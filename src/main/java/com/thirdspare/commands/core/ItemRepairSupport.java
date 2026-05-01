package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;

final class ItemRepairSupport {
    private ItemRepairSupport() {
    }

    static boolean repairHeldItem(PlayerRef playerRef) {
        Player player = playerRef.getComponent(Player.getComponentType());
        if (player == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        ItemContainer container = inventory.usingToolsItem() ? inventory.getTools() : inventory.getHotbar();
        short slot = inventory.usingToolsItem() ? inventory.getActiveToolsSlot() : inventory.getActiveHotbarSlot();
        ItemStack item = inventory.getItemInHand();
        if (!isRepairable(item)) {
            return false;
        }

        return container.setItemStackForSlot(slot, repair(item)).succeeded();
    }

    static int repairAll(PlayerRef playerRef) {
        Player player = playerRef.getComponent(Player.getComponentType());
        if (player == null) {
            return 0;
        }

        Inventory inventory = player.getInventory();
        List<SlotRepair> repairs = new ArrayList<>();
        collectRepairs(inventory.getHotbar(), repairs);
        collectRepairs(inventory.getTools(), repairs);
        collectRepairs(inventory.getUtility(), repairs);
        collectRepairs(inventory.getArmor(), repairs);
        collectRepairs(inventory.getStorage(), repairs);
        collectRepairs(inventory.getBackpack(), repairs);

        int repaired = 0;
        for (SlotRepair repair : repairs) {
            if (repair.container().setItemStackForSlot(repair.slot(), repair.item()).succeeded()) {
                repaired++;
            }
        }
        return repaired;
    }

    private static void collectRepairs(ItemContainer container, List<SlotRepair> repairs) {
        if (container == null) {
            return;
        }
        container.forEach((slot, item) -> {
            if (isRepairable(item)) {
                repairs.add(new SlotRepair(container, slot, repair(item)));
            }
        });
    }

    private static boolean isRepairable(ItemStack item) {
        return item != null
                && !item.isEmpty()
                && !item.isUnbreakable()
                && item.getMaxDurability() > 0.0D
                && item.getDurability() < item.getMaxDurability();
    }

    private static ItemStack repair(ItemStack item) {
        return item.withRestoredDurability(item.getMaxDurability());
    }

    private record SlotRepair(ItemContainer container, short slot, ItemStack item) {
    }
}
