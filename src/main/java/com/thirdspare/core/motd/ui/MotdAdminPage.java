package com.thirdspare.core.motd.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.core.motd.MotdManager;
import com.thirdspare.core.motd.data.MotdConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MotdAdminPage extends InteractiveCustomUIPage<MotdAdminPage.MotdAdminEventData> {
    private static final String[][] COLOR_PRESETS = {
            {"#F1BA50", "Gold"},
            {"#FFFFFF", "White"},
            {"#BDF2C3", "Green"},
            {"#7AB7FF", "Blue"},
            {"#FFB4B4", "Red"},
            {"#E6E6E6", "Gray"}
    };

    private final MotdManager manager;
    private String statusMessage = "";
    private boolean errorStatus;

    public MotdAdminPage(PlayerRef playerRef, MotdManager manager) {
        super(playerRef, CustomPageLifetime.CanDismiss, MotdAdminEventData.CODEC);
        this.manager = manager;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("MotdAdmin.ui");
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                MotdAdminEventData data) {
        if ("Save".equals(data.action)) {
            manager.update(data.enabled, data.showOnJoin, data.lines, data.titleColor, data.lineColor);
            setStatus(null, "Saved MOTD settings.");
        }

        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        renderColorDropdown(builder, "#TitleColorDropdown");
        renderColorDropdown(builder, "#LineColorDropdown");

        MotdConfig motd = manager.getMotd();
        builder.set("#EnabledCheck.Value", motd.isEnabled());
        builder.set("#ShowOnJoinCheck.Value", motd.isShowOnJoin());
        builder.set("#LinesInput.Value", manager.linesText());
        builder.set("#TitleColorDropdown.Value", motd.getTitleColor());
        builder.set("#LineColorDropdown.Value", motd.getLineColor());
        renderStatus(builder);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", new EventData()
                .append("Action", "Save")
                .append("@Enabled", "#EnabledCheck.Value")
                .append("@ShowOnJoin", "#ShowOnJoinCheck.Value")
                .append("@Lines", "#LinesInput.Value")
                .append("@TitleColor", "#TitleColorDropdown.Value")
                .append("@LineColor", "#LineColorDropdown.Value"), false);
    }

    private void renderColorDropdown(UICommandBuilder builder, String selector) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        for (String[] preset : COLOR_PRESETS) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(preset[1] + " (" + preset[0] + ")"), preset[0]));
        }
        builder.set(selector + ".Entries", entries);
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

    public static class MotdAdminEventData {
        public static final BuilderCodec<MotdAdminEventData> CODEC =
                BuilderCodec.builder(MotdAdminEventData.class, MotdAdminEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("@Enabled", Codec.BOOLEAN),
                                (data, value) -> data.enabled = value,
                                data -> data.enabled).add()
                        .append(new KeyedCodec<>("@ShowOnJoin", Codec.BOOLEAN),
                                (data, value) -> data.showOnJoin = value,
                                data -> data.showOnJoin).add()
                        .append(new KeyedCodec<>("@Lines", Codec.STRING),
                                (data, value) -> data.lines = value != null ? value : "",
                                data -> data.lines).add()
                        .append(new KeyedCodec<>("@TitleColor", Codec.STRING),
                                (data, value) -> data.titleColor = value != null ? value : "#F1BA50",
                                data -> data.titleColor).add()
                        .append(new KeyedCodec<>("@LineColor", Codec.STRING),
                                (data, value) -> data.lineColor = value != null ? value : "#FFFFFF",
                                data -> data.lineColor).add()
                        .build();

        private String action = "";
        private boolean enabled;
        private boolean showOnJoin;
        private String lines = "";
        private String titleColor = "#F1BA50";
        private String lineColor = "#FFFFFF";
    }
}
