package com.thirdspare.core.rules;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.core.rules.data.RulesConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RulesManager {
    private final Config<RulesConfig> config;
    private final RulesConfig rules;

    public RulesManager(Config<RulesConfig> config, RulesConfig rules) {
        this.config = config;
        this.rules = rules != null ? rules : new RulesConfig();
    }

    public RulesConfig getRules() {
        return rules;
    }

    public void sendTo(PlayerRef player) {
        if (player == null) {
            return;
        }
        player.sendMessage(Message.raw(rules.getTitle()).color("#F1BA50"));
        List<String> list = rules.getRules();
        if (list.isEmpty()) {
            player.sendMessage(Message.raw("No rules have been configured.").color("#BBBBBB"));
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            player.sendMessage(Message.raw((i + 1) + ". " + list.get(i)).color("#FFFFFF"));
        }
    }

    public CompletableFuture<Void> save() {
        return config.save();
    }

    public void update(String title, String rulesText) {
        rules.setTitle(title);
        rules.setRules(textToLines(rulesText));
        save();
    }

    public String rulesText() {
        return String.join("\n", rules.getRules());
    }

    static List<String> textToLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return text.lines().map(String::strip).filter(line -> !line.isBlank()).toList();
    }
}
