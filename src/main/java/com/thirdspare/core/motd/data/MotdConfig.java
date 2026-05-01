package com.thirdspare.core.motd.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.Arrays;
import java.util.List;

public class MotdConfig {
    private int schemaVersion;
    private boolean enabled;
    private String[] lines;
    private boolean showOnJoin;
    private String titleColor;
    private String lineColor;

    public MotdConfig() {
        this.schemaVersion = 1;
        this.enabled = true;
        this.lines = new String[]{"Welcome to the server!", "Use /rules to view the server rules."};
        this.showOnJoin = true;
        this.titleColor = "#F1BA50";
        this.lineColor = "#FFFFFF";
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = Math.max(1, schemaVersion);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String[] getLinesArray() {
        return lines;
    }

    public void setLinesArray(String[] lines) {
        this.lines = lines != null ? lines : new String[0];
    }

    public List<String> getLines() {
        return Arrays.asList(lines != null ? lines : new String[0]);
    }

    public void setLines(List<String> lines) {
        this.lines = lines != null ? lines.toArray(String[]::new) : new String[0];
    }

    public boolean isShowOnJoin() {
        return showOnJoin;
    }

    public void setShowOnJoin(boolean showOnJoin) {
        this.showOnJoin = showOnJoin;
    }

    public String getTitleColor() {
        return titleColor == null || titleColor.isBlank() ? "#F1BA50" : titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = normalizeColor(titleColor, "#F1BA50");
    }

    public String getLineColor() {
        return lineColor == null || lineColor.isBlank() ? "#FFFFFF" : lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = normalizeColor(lineColor, "#FFFFFF");
    }

    private static String normalizeColor(String color, String fallback) {
        if (color == null || color.isBlank()) {
            return fallback;
        }
        String trimmed = color.trim();
        return trimmed.startsWith("#") ? trimmed : "#" + trimmed;
    }

    public static final BuilderCodec<MotdConfig> CODEC = BuilderCodec.builder(MotdConfig.class, MotdConfig::new)
            .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER), MotdConfig::setSchemaVersion, MotdConfig::getSchemaVersion).add()
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), MotdConfig::setEnabled, MotdConfig::isEnabled).add()
            .append(new KeyedCodec<>("Lines", Codec.STRING_ARRAY), MotdConfig::setLinesArray, MotdConfig::getLinesArray).add()
            .append(new KeyedCodec<>("ShowOnJoin", Codec.BOOLEAN), MotdConfig::setShowOnJoin, MotdConfig::isShowOnJoin).add()
            .append(new KeyedCodec<>("TitleColor", Codec.STRING), MotdConfig::setTitleColor, MotdConfig::getTitleColor).add()
            .append(new KeyedCodec<>("LineColor", Codec.STRING), MotdConfig::setLineColor, MotdConfig::getLineColor).add()
            .build();
}
