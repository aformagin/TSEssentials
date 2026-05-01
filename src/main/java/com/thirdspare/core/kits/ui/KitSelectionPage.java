package com.thirdspare.core.kits.ui;

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
import com.thirdspare.core.kits.KitService;
import com.thirdspare.core.kits.data.KitDefinition;

import javax.annotation.Nonnull;
import java.util.List;

public class KitSelectionPage extends InteractiveCustomUIPage<KitSelectionPage.KitSelectionEventData> {
    private static final int MAX_KITS = 10;

    private final PlayerRef player;
    private final KitService service;
    private String statusMessage = "";
    private boolean errorStatus;

    public KitSelectionPage(PlayerRef player, KitService service) {
        super(player, CustomPageLifetime.CanDismiss, KitSelectionEventData.CODEC);
        this.player = player;
        this.service = service;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("KitSelection.ui");
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                KitSelectionEventData data) {
        if ("Grant".equals(data.action)) {
            KitService.GrantResult result = service.grant(player, data.kit);
            setStatus(result.success() ? null : result.message(), result.success() ? result.message() : null);
        }

        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        List<KitDefinition> kits = service.accessibleKits(player);
        builder.set("#EmptyMessage.Visible", kits.isEmpty());
        for (int i = 0; i < MAX_KITS; i++) {
            String selector = "#KitRow" + i;
            if (i < kits.size()) {
                KitDefinition kit = kits.get(i);
                builder.set(selector + ".Visible", true);
                builder.set(selector + ".Text", rowLabel(kit));
                events.addEventBinding(CustomUIEventBindingType.Activating, selector, new EventData()
                        .append("Action", "Grant")
                        .append("Kit", kit.getName()), false);
            } else {
                builder.set(selector + ".Visible", false);
                builder.set(selector + ".Text", "");
            }
        }
        renderStatus(builder);
    }

    private String rowLabel(KitDefinition kit) {
        String cooldown = kit.getCooldownSeconds() > 0L ? " cooldown " + kit.getCooldownSeconds() + "s" : "";
        return kit.getDisplayName() + " (" + kit.getItems().size() + " item(s))" + cooldown;
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

    public static class KitSelectionEventData {
        public static final BuilderCodec<KitSelectionEventData> CODEC =
                BuilderCodec.builder(KitSelectionEventData.class, KitSelectionEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("Kit", Codec.STRING),
                                (data, value) -> data.kit = value != null ? value : "",
                                data -> data.kit).add()
                        .build();

        private String action = "";
        private String kit = "";
    }
}
