package com.thirdspare.modules.economy.ui;

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
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.economy.EconomyManager;
import com.thirdspare.modules.economy.EconomyService;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EconomyPage extends InteractiveCustomUIPage<EconomyPage.EconomyEventData> {
    private final PlayerRef playerRef;
    private final EconomyService economyService;
    private final EconomyManager economyManager;
    private final TSEUiDocument uiDocument;
    private String statusMessage = "";
    private boolean errorStatus;

    public EconomyPage(PlayerRef playerRef, EconomyService economyService, TSEUiDocument uiDocument) {
        super(playerRef, CustomPageLifetime.CanDismiss, EconomyEventData.CODEC);
        this.playerRef = playerRef;
        this.economyService = economyService;
        this.economyManager = economyService.getEconomyManager();
        this.uiDocument = Objects.requireNonNull(uiDocument, "uiDocument");
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        uiDocument.appendTo(builder);
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                EconomyEventData data) {
        if ("Pay".equals(data.action)) {
            pay(data);
        }

        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        EconomyService.AccountView account = economyService.getAccountView(playerRef);
        builder.set("#PlayerName.Text", account.displayName());
        builder.set("#BalanceValue.Text", economyManager.format(account.balance()));

        events.addEventBinding(CustomUIEventBindingType.Activating, "#PayButton", new EventData()
                .append("Action", "Pay")
                .append("@Recipient", "#RecipientInput.Value")
                .append("@Amount", "#AmountInput.Value"), false);

        renderStatus(builder);
    }

    private void pay(EconomyEventData data) {
        EconomyManager.ParseResult amount = economyManager.parseAmount(data.amount);
        if (!amount.success()) {
            setStatus("Invalid amount: " + amount.errorMessage(), null);
            return;
        }

        EconomyService.TransferResult result = economyService.transfer(playerRef, data.recipient, amount.amount());
        if (!result.success()) {
            setStatus(result.error(), null);
            return;
        }

        String formattedAmount = economyManager.format(amount.amount());
        setStatus(null, "Paid " + result.recipient().displayName() + " " + formattedAmount + ".");
        PlayerRef recipientRef = result.recipient().onlinePlayer();
        if (recipientRef != null && recipientRef.isValid()) {
            recipientRef.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                    playerRef.getUsername() + " paid you " + formattedAmount + ".").color("#8DE969"));
        }
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

    public static class EconomyEventData {
        public static final BuilderCodec<EconomyEventData> CODEC =
                BuilderCodec.builder(EconomyEventData.class, EconomyEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("@Recipient", Codec.STRING),
                                (data, value) -> data.recipient = value != null ? value : "",
                                data -> data.recipient).add()
                        .append(new KeyedCodec<>("@Amount", Codec.STRING),
                                (data, value) -> data.amount = value != null ? value : "",
                                data -> data.amount).add()
                        .build();

        private String action = "";
        private String recipient = "";
        private String amount = "";
    }
}
