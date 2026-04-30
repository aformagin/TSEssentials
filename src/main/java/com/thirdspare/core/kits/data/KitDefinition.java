package com.thirdspare.core.kits.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import java.util.Arrays;
import java.util.List;

public class KitDefinition {
    private String name;
    private String displayName;
    private String description;
    private String permission;
    private long cooldownSeconds;
    private KitItemDefinition[] items;
    private boolean enabled;

    public KitDefinition() {
        this.name = "";
        this.displayName = "";
        this.description = "";
        this.permission = "";
        this.cooldownSeconds = 0L;
        this.items = new KitItemDefinition[0];
        this.enabled = true;
    }

    public KitDefinition(String name) {
        this();
        setName(name);
        setDisplayName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeName(name);
    }

    public String getDisplayName() {
        return displayName == null || displayName.isBlank() ? name : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission != null ? permission.trim() : "";
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = Math.max(0L, cooldownSeconds);
    }

    public KitItemDefinition[] getItemsArray() {
        return items;
    }

    public void setItemsArray(KitItemDefinition[] items) {
        this.items = items != null ? items : new KitItemDefinition[0];
    }

    public List<KitItemDefinition> getItems() {
        return Arrays.asList(items != null ? items : new KitItemDefinition[0]);
    }

    public void setItems(List<KitItemDefinition> items) {
        this.items = items != null ? items.toArray(KitItemDefinition[]::new) : new KitItemDefinition[0];
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
    }

    public static final BuilderCodec<KitDefinition> CODEC = BuilderCodec.builder(KitDefinition.class, KitDefinition::new)
            .append(new KeyedCodec<>("Name", Codec.STRING), KitDefinition::setName, KitDefinition::getName).add()
            .append(new KeyedCodec<>("DisplayName", Codec.STRING), KitDefinition::setDisplayName, KitDefinition::getDisplayName).add()
            .append(new KeyedCodec<>("Description", Codec.STRING), KitDefinition::setDescription, KitDefinition::getDescription).add()
            .append(new KeyedCodec<>("Permission", Codec.STRING), KitDefinition::setPermission, KitDefinition::getPermission).add()
            .append(new KeyedCodec<>("CooldownSeconds", Codec.LONG), KitDefinition::setCooldownSeconds, KitDefinition::getCooldownSeconds).add()
            .append(new KeyedCodec<>("Items", ArrayCodec.ofBuilderCodec(KitItemDefinition.CODEC, KitItemDefinition[]::new)), KitDefinition::setItemsArray, KitDefinition::getItemsArray).add()
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), KitDefinition::setEnabled, KitDefinition::isEnabled).add()
            .build();
}
