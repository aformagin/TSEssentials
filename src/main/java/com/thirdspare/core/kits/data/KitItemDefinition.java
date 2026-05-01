package com.thirdspare.core.kits.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class KitItemDefinition {
    private String itemId;
    private int quantity;

    public KitItemDefinition() {
        this.itemId = "";
        this.quantity = 1;
    }

    public KitItemDefinition(String itemId, int quantity) {
        setItemId(itemId);
        setQuantity(quantity);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId != null ? itemId : "";
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public boolean isValid() {
        return itemId != null && !itemId.isBlank() && quantity > 0;
    }

    public ItemStack toItemStack() {
        return new ItemStack(itemId, quantity);
    }

    public static KitItemDefinition fromItemStack(ItemStack item) {
        return new KitItemDefinition(item.getItemId(), item.getQuantity());
    }

    public static final BuilderCodec<KitItemDefinition> CODEC = BuilderCodec.builder(KitItemDefinition.class, KitItemDefinition::new)
            .append(new KeyedCodec<>("ItemId", Codec.STRING), KitItemDefinition::setItemId, KitItemDefinition::getItemId).add()
            .append(new KeyedCodec<>("Quantity", Codec.INTEGER), KitItemDefinition::setQuantity, KitItemDefinition::getQuantity).add()
            .build();
}
