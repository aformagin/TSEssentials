package com.thirdspare.core.motd;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.core.motd.data.MotdConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MotdManager {
    private final Config<MotdConfig> config;
    private final MotdConfig motd;

    public MotdManager(Config<MotdConfig> config, MotdConfig motd) {
        this.config = config;
        this.motd = motd != null ? motd : new MotdConfig();
    }

    public MotdConfig getMotd() {
        return motd;
    }

    public void sendTo(PlayerRef player, boolean ignoreJoinSetting) {
        if (player == null || !motd.isEnabled()) {
            return;
        }
        if (!ignoreJoinSetting && !motd.isShowOnJoin()) {
            return;
        }
        List<String> lines = motd.getLines();
        if (lines.isEmpty()) {
            return;
        }
        player.sendMessage(Message.raw("Message of the Day").color(motd.getTitleColor()));
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                player.sendMessage(Message.raw(line).color(motd.getLineColor()));
            }
        }
    }

    public CompletableFuture<Void> save() {
        return config.save();
    }

    public void update(boolean enabled, boolean showOnJoin, String linesText) {
        motd.setEnabled(enabled);
        motd.setShowOnJoin(showOnJoin);
        motd.setLines(textToLines(linesText));
        save();
    }

    public void update(boolean enabled, boolean showOnJoin, String linesText, String titleColor, String lineColor) {
        motd.setEnabled(enabled);
        motd.setShowOnJoin(showOnJoin);
        motd.setLines(textToLines(linesText));
        motd.setTitleColor(titleColor);
        motd.setLineColor(lineColor);
        save();
    }

    public String linesText() {
        return String.join("\n", motd.getLines());
    }

    static List<String> textToLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return text.lines().map(String::strip).filter(line -> !line.isBlank()).toList();
    }
}
