package com.thirdspare.modules.economy.ui;

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
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.economy.EconomyManager;
import com.thirdspare.modules.economy.EconomyService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EconomyAdminPage extends InteractiveCustomUIPage<EconomyAdminPage.EconomyAdminEventData> {
    private static final int MAX_VISIBLE_ACCOUNT_ROWS = 10;

    private final PlayerRef playerRef;
    private final EconomyService economyService;
    private final EconomyManager economyManager;
    private final TSEUiDocument uiDocument;
    private String selectedTarget = "";
    private String statusMessage = "";
    private boolean errorStatus;

    public EconomyAdminPage(PlayerRef playerRef, EconomyService economyService, TSEUiDocument uiDocument) {
        super(playerRef, CustomPageLifetime.CanDismiss, EconomyAdminEventData.CODEC);
        this.playerRef = playerRef;
        this.economyService = economyService;
        this.economyManager = economyService.getEconomyManager();
        this.uiDocument = Objects.requireNonNull(uiDocument, "uiDocument");
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        uiDocument.appendTo(builder);
        ensureSelection();
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                EconomyAdminEventData data) {
        switch (data.action) {
            case "Select" -> selectedTarget = data.target;
            case "Apply" -> applyMutation(data);
            default -> {
                statusMessage = "";
                errorStatus = false;
            }
        }

        ensureSelection();
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        List<EconomyService.AccountView> accounts = economyService.getAccountViews(MAX_VISIBLE_ACCOUNT_ROWS);
        for (int i = 0; i < MAX_VISIBLE_ACCOUNT_ROWS; i++) {
            String rowId = "#AccountRow" + i;
            if (i < accounts.size()) {
                EconomyService.AccountView account = accounts.get(i);
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", rowLabel(account));
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        rowId,
                        new EventData()
                                .append("Action", "Select")
                                .append("Target", account.uuid().toString()),
                        false
                );
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }

        renderActionDropdown(builder);
        renderSelected(builder);
        renderStatus(builder);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ApplyButton", new EventData()
                .append("Action", "Apply")
                .append("@Target", "#TargetInput.Value")
                .append("@Amount", "#AmountInput.Value")
                .append("@Mutation", "#ActionDropdown.Value"), false);
    }

    private void renderActionDropdown(UICommandBuilder builder) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        entries.add(new DropdownEntryInfo(LocalizableString.fromString("Give"), "give"));
        entries.add(new DropdownEntryInfo(LocalizableString.fromString("Take"), "take"));
        entries.add(new DropdownEntryInfo(LocalizableString.fromString("Set"), "set"));
        builder.set("#ActionDropdown.Entries", entries);
        builder.set("#ActionDropdown.Value", "give");
    }

    private void renderSelected(UICommandBuilder builder) {
        EconomyService.AccountView account = selectedTarget == null || selectedTarget.isBlank()
                ? null
                : economyService.getAccountView(selectedTarget);
        if (account == null) {
            builder.set("#SelectedTitle.Text", "No account selected");
            builder.set("#TargetInput.Value", "");
            builder.set("#CurrentBalance.Text", "");
            return;
        }

        builder.set("#SelectedTitle.Text", account.displayName());
        builder.set("#TargetInput.Value", account.uuid().toString());
        builder.set("#CurrentBalance.Text", "Current balance: " + economyManager.format(account.balance()));
    }

    private void applyMutation(EconomyAdminEventData data) {
        EconomyManager.ParseResult amount = economyManager.parseAmount(data.amount);
        if (!amount.success()) {
            setStatus("Invalid amount: " + amount.errorMessage(), null);
            return;
        }

        String target = data.target == null || data.target.isBlank() ? selectedTarget : data.target;
        if (!"give".equals(data.mutation) && !"take".equals(data.mutation) && !"set".equals(data.mutation)) {
            setStatus("Choose give, take, or set.", null);
            return;
        }

        String permission = switch (data.mutation) {
            case "give" -> TSEssentialsPermissions.ECO_GIVE;
            case "take" -> TSEssentialsPermissions.ECO_TAKE;
            case "set" -> TSEssentialsPermissions.ECO_SET;
            default -> throw new IllegalStateException("Unexpected action: " + data.mutation);
        };
        if (!CommandUtils.hasPermission(playerRef, permission)) {
            setStatus("You do not have permission to use /eco " + data.mutation + ".", null);
            return;
        }

        EconomyService.MutationResult result = switch (data.mutation) {
            case "give" -> economyService.deposit(target, amount.amount());
            case "take" -> economyService.withdraw(target, amount.amount());
            case "set" -> economyService.setBalance(target, amount.amount());
            default -> throw new IllegalStateException("Unexpected action: " + data.mutation);
        };

        if (!result.success()) {
            setStatus(result.error(), null);
            return;
        }

        selectedTarget = result.account().uuid().toString();
        String formatted = economyManager.format(amount.amount());
        String success = switch (data.mutation) {
            case "give" -> "Gave " + result.account().displayName() + " " + formatted + ".";
            case "take" -> "Took " + formatted + " from " + result.account().displayName() + ".";
            case "set" -> "Set " + result.account().displayName() + " to " + economyManager.format(result.account().balance()) + ".";
            default -> "Updated account.";
        };
        setStatus(null, success);
    }

    private String rowLabel(EconomyService.AccountView account) {
        String selectedMarker = account.uuid().toString().equals(selectedTarget) ? "> " : "  ";
        String onlineMarker = account.online() ? " online" : "";
        return selectedMarker + account.displayName() + " " + economyManager.format(account.balance()) + onlineMarker;
    }

    private void ensureSelection() {
        if (selectedTarget != null && !selectedTarget.isBlank() && economyService.getAccountView(selectedTarget) != null) {
            return;
        }
        List<EconomyService.AccountView> accounts = economyService.getAccountViews(1);
        selectedTarget = accounts.isEmpty() ? "" : accounts.getFirst().uuid().toString();
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

    public static class EconomyAdminEventData {
        public static final BuilderCodec<EconomyAdminEventData> CODEC =
                BuilderCodec.builder(EconomyAdminEventData.class, EconomyAdminEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("Target", Codec.STRING),
                                (data, value) -> data.target = value != null ? value : "",
                                data -> data.target).add()
                        .append(new KeyedCodec<>("@Target", Codec.STRING),
                                (data, value) -> data.target = value != null ? value : data.target,
                                data -> data.target).add()
                        .append(new KeyedCodec<>("@Amount", Codec.STRING),
                                (data, value) -> data.amount = value != null ? value : "",
                                data -> data.amount).add()
                        .append(new KeyedCodec<>("@Mutation", Codec.STRING),
                                (data, value) -> data.mutation = value != null ? value : "give",
                                data -> data.mutation).add()
                        .build();

        private String action = "";
        private String target = "";
        private String amount = "";
        private String mutation = "give";
    }
}
