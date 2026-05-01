package com.thirdspare.core.kits.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;
import java.util.Map;

public class KitsConfig {
    private int schemaVersion;
    private Map<String, KitDefinition> kits;

    public KitsConfig() {
        this.schemaVersion = 1;
        this.kits = new HashMap<>();
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = Math.max(1, schemaVersion);
    }

    public Map<String, KitDefinition> getKits() {
        return kits;
    }

    public void setKits(Map<String, KitDefinition> kits) {
        this.kits = kits != null ? new HashMap<>(kits) : new HashMap<>();
        normalizeKeys();
    }

    public KitDefinition getKit(String name) {
        return kits.get(KitDefinition.normalizeName(name));
    }

    public void setKit(KitDefinition kit) {
        if (kit != null && !kit.getName().isBlank()) {
            kits.put(kit.getName(), kit);
        }
    }

    public boolean removeKit(String name) {
        return kits.remove(KitDefinition.normalizeName(name)) != null;
    }

    public void normalizeKeys() {
        Map<String, KitDefinition> normalized = new HashMap<>();
        for (KitDefinition kit : kits.values()) {
            if (kit != null) {
                kit.setName(kit.getName());
                if (!kit.getName().isBlank()) {
                    normalized.put(kit.getName(), kit);
                }
            }
        }
        kits = normalized;
    }

    public static final BuilderCodec<KitsConfig> CODEC = BuilderCodec.builder(KitsConfig.class, KitsConfig::new)
            .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER), KitsConfig::setSchemaVersion, KitsConfig::getSchemaVersion).add()
            .append(new KeyedCodec<>("Kits", new MapCodec<>(KitDefinition.CODEC, HashMap::new)), KitsConfig::setKits, KitsConfig::getKits).add()
            .build();
}
