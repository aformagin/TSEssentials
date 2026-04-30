package com.thirdspare.core.rules.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.Arrays;
import java.util.List;

public class RulesConfig {
    private int schemaVersion;
    private String title;
    private String[] rules;

    public RulesConfig() {
        this.schemaVersion = 1;
        this.title = "Server Rules";
        this.rules = new String[]{"Be respectful.", "No cheating or exploiting.", "Follow staff instructions."};
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = Math.max(1, schemaVersion);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null && !title.isBlank() ? title : "Server Rules";
    }

    public String[] getRulesArray() {
        return rules;
    }

    public void setRulesArray(String[] rules) {
        this.rules = rules != null ? rules : new String[0];
    }

    public List<String> getRules() {
        return Arrays.asList(rules != null ? rules : new String[0]);
    }

    public void setRules(List<String> rules) {
        this.rules = rules != null ? rules.toArray(String[]::new) : new String[0];
    }

    public static final BuilderCodec<RulesConfig> CODEC = BuilderCodec.builder(RulesConfig.class, RulesConfig::new)
            .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER), RulesConfig::setSchemaVersion, RulesConfig::getSchemaVersion).add()
            .append(new KeyedCodec<>("Title", Codec.STRING), RulesConfig::setTitle, RulesConfig::getTitle).add()
            .append(new KeyedCodec<>("Rules", Codec.STRING_ARRAY), RulesConfig::setRulesArray, RulesConfig::getRulesArray).add()
            .build();
}
