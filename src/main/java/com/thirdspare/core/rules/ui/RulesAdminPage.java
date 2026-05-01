package com.thirdspare.core.rules.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.core.rules.RulesManager;

import javax.annotation.Nonnull;

public class RulesAdminPage extends InteractiveCustomUIPage<RulesAdminPage.RulesAdminEventData> {
    private final RulesManager manager;
    private String statusMessage = "";
    private boolean errorStatus;

    public RulesAdminPage(PlayerRef playerRef, RulesManager manager) {
        super(playerRef, CustomPageLifetime.CanDismiss, RulesAdminEventData.CODEC);
        this.manager = manager;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("RulesAdmin.ui");
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                RulesAdminEventData data) {
        if ("Save".equals(data.action)) {
            manager.update(data.title, data.rules);
            setStatus(null, "Saved server rules.");
        }

        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        builder.set("#TitleInput.Value", manager.getRules().getTitle());
        builder.set("#RulesInput.Value", manager.rulesText());
        renderStatus(builder);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", new EventData()
                .append("Action", "Save")
                .append("@Title", "#TitleInput.Value")
                .append("@Rules", "#RulesInput.Value"), false);
    }

    private void renderStatus(UICommandBuilder builder) {
        builder.set("#StatusMessage.Visible", statusMessage != null && !statusMessage.isBlank());
        builder.set("#StatusMessage.Text", statusMessage);
        builder.set("#StatusMessage.Style.TextColor", errorStatus ? "#FFB4B4" : "#BDF2C3");
    }

    private void setStatus(String error, String success) {
        errorStatus = error != null;
        statusMessage = error != null ? error : success;
    }

    public static class RulesAdminEventData {
        public static final BuilderCodec<RulesAdminEventData> CODEC =
                BuilderCodec.builder(RulesAdminEventData.class, RulesAdminEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("@Title", Codec.STRING),
                                (data, value) -> data.title = value != null ? value : "",
                                data -> data.title).add()
                        .append(new KeyedCodec<>("@Rules", Codec.STRING),
                                (data, value) -> data.rules = value != null ? value : "",
                                data -> data.rules).add()
                        .build();

        private String action = "";
        private String title = "";
        private String rules = "";
    }
}
