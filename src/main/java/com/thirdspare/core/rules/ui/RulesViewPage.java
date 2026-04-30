package com.thirdspare.core.rules.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.rules.RulesManager;

import java.util.List;

public class RulesViewPage extends BasicCustomUIPage {
    private static final int MAX_RULES = 10;
    private final RulesManager manager;

    public RulesViewPage(PlayerRef playerRef, RulesManager manager) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.manager = manager;
    }

    @Override
    public void build(UICommandBuilder builder) {
        builder.append("RulesView.ui");
        builder.set("#Title.Text", manager.getRules().getTitle());
        List<String> rules = manager.getRules().getRules();
        builder.set("#EmptyMessage.Visible", rules.isEmpty());
        for (int i = 0; i < MAX_RULES; i++) {
            String selector = "#RuleRow" + i;
            if (i < rules.size()) {
                builder.set(selector + ".Visible", true);
                builder.set(selector + ".Text", (i + 1) + ". " + rules.get(i));
            } else {
                builder.set(selector + ".Visible", false);
                builder.set(selector + ".Text", "");
            }
        }
    }
}
