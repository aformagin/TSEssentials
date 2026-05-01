package com.thirdspare.core.kits;

import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.core.kits.data.KitDefinition;
import com.thirdspare.core.kits.data.KitItemDefinition;
import com.thirdspare.core.kits.data.KitsConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KitManager {
    private final Config<KitsConfig> config;
    private final KitsConfig kits;

    public KitManager(Config<KitsConfig> config, KitsConfig kits) {
        this.config = config;
        this.kits = kits != null ? kits : new KitsConfig();
        this.kits.normalizeKeys();
    }

    public KitDefinition getKit(String name) {
        return kits.getKit(name);
    }

    public List<KitDefinition> getKits() {
        List<KitDefinition> result = new ArrayList<>(kits.getKits().values());
        result.sort(Comparator.comparing(KitDefinition::getName));
        return result;
    }

    public String createKit(String name) {
        String normalized = KitDefinition.normalizeName(name);
        if (normalized.isBlank()) {
            return "Kit names may only contain letters, numbers, underscores, and hyphens.";
        }
        if (kits.getKit(normalized) != null) {
            return "A kit named '" + normalized + "' already exists.";
        }
        kits.setKit(new KitDefinition(normalized));
        save();
        return null;
    }

    public String deleteKit(String name) {
        if (!kits.removeKit(name)) {
            return "Unknown kit: " + name;
        }
        save();
        return null;
    }

    public String updateKit(String name, String displayName, String description, String permission, long cooldown, boolean enabled) {
        KitDefinition kit = kits.getKit(name);
        if (kit == null) {
            return "Unknown kit: " + name;
        }
        kit.setDisplayName(displayName);
        kit.setDescription(description);
        kit.setPermission(permission);
        kit.setCooldownSeconds(cooldown);
        kit.setEnabled(enabled);
        save();
        return null;
    }

    public String captureKit(String name, Inventory inventory) {
        KitDefinition kit = kits.getKit(name);
        if (kit == null) {
            return "Unknown kit: " + name;
        }
        List<KitItemDefinition> items = new ArrayList<>();
        capture(inventory.getHotbar(), items);
        capture(inventory.getTools(), items);
        capture(inventory.getUtility(), items);
        capture(inventory.getArmor(), items);
        capture(inventory.getStorage(), items);
        capture(inventory.getBackpack(), items);
        kit.setItems(items);
        save();
        return null;
    }

    public String clearItems(String name) {
        KitDefinition kit = kits.getKit(name);
        if (kit == null) {
            return "Unknown kit: " + name;
        }
        kit.setItems(List.of());
        save();
        return null;
    }

    public String addItem(String name, String itemId, int quantity) {
        KitDefinition kit = kits.getKit(name);
        if (kit == null) {
            return "Unknown kit: " + name;
        }
        if (itemId == null || itemId.isBlank()) {
            return "Choose an item before adding it.";
        }
        List<KitItemDefinition> items = new ArrayList<>(kit.getItems());
        items.add(new KitItemDefinition(itemId.trim(), Math.max(1, quantity)));
        kit.setItems(items);
        save();
        return null;
    }

    public CompletableFuture<Void> save() {
        kits.normalizeKeys();
        return config.save();
    }

    private void capture(ItemContainer container, List<KitItemDefinition> items) {
        if (container == null) {
            return;
        }
        container.forEach((slot, item) -> {
            if (item != null && !item.isEmpty() && item.getQuantity() > 0) {
                items.add(KitItemDefinition.fromItemStack(item));
            }
        });
    }

    public List<ItemStack> toItemStacks(KitDefinition kit) {
        return kit.getItems().stream()
                .filter(KitItemDefinition::isValid)
                .map(KitItemDefinition::toItemStack)
                .toList();
    }
}
