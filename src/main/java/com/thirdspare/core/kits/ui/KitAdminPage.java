package com.thirdspare.core.kits.ui;

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
import com.thirdspare.core.kits.KitManager;
import com.thirdspare.core.kits.KitService;
import com.thirdspare.core.kits.data.KitDefinition;
import com.thirdspare.core.kits.data.KitItemDefinition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class KitAdminPage extends InteractiveCustomUIPage<KitAdminPage.KitAdminEventData> {
    private static final int MAX_KIT_ROWS = 10;
    private static final int MAX_ITEM_ROWS = 8;
    private static final String[][] ITEM_PRESETS = {
            {"Furniture_Construction_Sign", "Construction Sign"},
            {"Furniture_Tavern_Bed", "Tavern Bed"},
            {"Furniture_Village_Brazier", "Village Brazier"},
            {"Furniture_Village_Statue", "Village Statue"}
    };

    private final KitManager manager;
    private String selectedKit;
    private String statusMessage = "";
    private boolean errorStatus;

    public KitAdminPage(PlayerRef playerRef, KitService service) {
        super(playerRef, CustomPageLifetime.CanDismiss, KitAdminEventData.CODEC);
        this.manager = service.getManager();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("KitAdmin.ui");
        ensureSelection();
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                KitAdminEventData data) {
        switch (data.action) {
            case "Select" -> selectedKit = KitDefinition.normalizeName(data.kit);
            case "Create" -> create(data.name);
            case "Save" -> saveSelected(data);
            case "Delete" -> deleteSelected();
            case "AddItem" -> addItem(data);
            case "ClearItems" -> clearItems();
            default -> {
            }
        }
        ensureSelection();
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        List<KitDefinition> kits = manager.getKits();
        for (int i = 0; i < MAX_KIT_ROWS; i++) {
            String selector = "#KitRow" + i;
            if (i < kits.size()) {
                KitDefinition kit = kits.get(i);
                builder.set(selector + ".Visible", true);
                builder.set(selector + ".Text", rowLabel(kit));
                events.addEventBinding(CustomUIEventBindingType.Activating, selector, new EventData()
                        .append("Action", "Select")
                        .append("Kit", kit.getName()), false);
            } else {
                builder.set(selector + ".Visible", false);
                builder.set(selector + ".Text", "");
            }
        }

        renderItemDropdown(builder);
        renderSelected(builder);
        renderStatus(builder);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateButton", new EventData()
                .append("Action", "Create")
                .append("@Name", "#CreateNameInput.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", new EventData()
                .append("Action", "Save")
                .append("@Name", "#NameInput.Value")
                .append("@DisplayName", "#DisplayNameInput.Value")
                .append("@Description", "#DescriptionInput.Value")
                .append("@Permission", "#PermissionInput.Value")
                .append("@Cooldown", "#CooldownInput.Value")
                .append("@Enabled", "#EnabledCheck.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#DeleteButton", EventData.of("Action", "Delete"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddItemButton", new EventData()
                .append("Action", "AddItem")
                .append("@ItemId", "#ItemDropdown.Value")
                .append("@Quantity", "#QuantityInput.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ClearItemsButton", EventData.of("Action", "ClearItems"), false);
    }

    private void renderSelected(UICommandBuilder builder) {
        KitDefinition kit = manager.getKit(selectedKit);
        if (kit == null) {
            builder.set("#SelectedTitle.Text", "No kit selected");
            builder.set("#NameInput.Value", "");
            builder.set("#DisplayNameInput.Value", "");
            builder.set("#DescriptionInput.Value", "");
            builder.set("#PermissionInput.Value", "");
            builder.set("#CooldownInput.Value", 0.0D);
            builder.set("#EnabledCheck.Value", false);
            renderItems(builder, List.of());
            return;
        }
        builder.set("#SelectedTitle.Text", kit.getDisplayName());
        builder.set("#NameInput.Value", kit.getName());
        builder.set("#DisplayNameInput.Value", kit.getDisplayName());
        builder.set("#DescriptionInput.Value", kit.getDescription());
        builder.set("#PermissionInput.Value", kit.getPermission());
        builder.set("#CooldownInput.Value", (double) kit.getCooldownSeconds());
        builder.set("#EnabledCheck.Value", kit.isEnabled());
        renderItems(builder, kit.getItems());
    }

    private void renderItems(UICommandBuilder builder, List<KitItemDefinition> items) {
        for (int i = 0; i < MAX_ITEM_ROWS; i++) {
            String selector = "#ItemRow" + i;
            if (i < items.size()) {
                KitItemDefinition item = items.get(i);
                builder.set(selector + ".Visible", true);
                builder.set(selector + ".Text", item.getQuantity() + "x " + item.getItemId());
            } else {
                builder.set(selector + ".Visible", false);
                builder.set(selector + ".Text", "");
            }
        }
    }

    private void renderItemDropdown(UICommandBuilder builder) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        for (String[] preset : ITEM_PRESETS) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(preset[1]), preset[0]));
        }
        builder.set("#ItemDropdown.Entries", entries);
        builder.set("#ItemDropdown.Value", ITEM_PRESETS[0][0]);
    }

    private String rowLabel(KitDefinition kit) {
        String selected = kit.getName().equals(selectedKit) ? "> " : "";
        String disabled = kit.isEnabled() ? "" : " disabled";
        return selected + kit.getName() + " (" + kit.getItems().size() + " item(s))" + disabled;
    }

    private void create(String name) {
        String error = manager.createKit(name);
        if (error == null) {
            selectedKit = KitDefinition.normalizeName(name);
        }
        setStatus(error, "Created kit " + selectedKit + ".");
    }

    private void saveSelected(KitAdminEventData data) {
        KitDefinition current = manager.getKit(selectedKit);
        if (current == null) {
            setStatus("Select a kit before saving.", null);
            return;
        }
        String newName = KitDefinition.normalizeName(data.name);
        if (!current.getName().equals(newName)) {
            String createError = manager.createKit(newName);
            if (createError != null) {
                setStatus(createError, null);
                return;
            }
            KitDefinition renamed = manager.getKit(newName);
            renamed.setItems(current.getItems());
            manager.deleteKit(current.getName());
            selectedKit = newName;
        }
        String error = manager.updateKit(selectedKit, data.displayName, data.description, data.permission,
                Math.max(0L, Math.round(data.cooldown)), data.enabled);
        setStatus(error, "Saved kit " + selectedKit + ".");
    }

    private void deleteSelected() {
        String deleted = selectedKit;
        String error = manager.deleteKit(selectedKit);
        if (error == null) {
            selectedKit = null;
        }
        setStatus(error, "Deleted kit " + deleted + ".");
    }

    private void addItem(KitAdminEventData data) {
        String error = manager.addItem(selectedKit, data.itemId, Math.max(1, (int) Math.round(data.quantity)));
        setStatus(error, "Added item to kit.");
    }

    private void clearItems() {
        String error = manager.clearItems(selectedKit);
        setStatus(error, "Cleared kit items.");
    }

    private void ensureSelection() {
        if (selectedKit != null && manager.getKit(selectedKit) != null) {
            return;
        }
        List<KitDefinition> kits = manager.getKits();
        selectedKit = kits.isEmpty() ? null : kits.getFirst().getName();
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

    public static class KitAdminEventData {
        public static final BuilderCodec<KitAdminEventData> CODEC =
                BuilderCodec.builder(KitAdminEventData.class, KitAdminEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("Kit", Codec.STRING),
                                (data, value) -> data.kit = value != null ? value : "",
                                data -> data.kit).add()
                        .append(new KeyedCodec<>("@Name", Codec.STRING),
                                (data, value) -> data.name = value != null ? value : "",
                                data -> data.name).add()
                        .append(new KeyedCodec<>("@DisplayName", Codec.STRING),
                                (data, value) -> data.displayName = value != null ? value : "",
                                data -> data.displayName).add()
                        .append(new KeyedCodec<>("@Description", Codec.STRING),
                                (data, value) -> data.description = value != null ? value : "",
                                data -> data.description).add()
                        .append(new KeyedCodec<>("@Permission", Codec.STRING),
                                (data, value) -> data.permission = value != null ? value : "",
                                data -> data.permission).add()
                        .append(new KeyedCodec<>("@Cooldown", Codec.DOUBLE),
                                (data, value) -> data.cooldown = value,
                                data -> data.cooldown).add()
                        .append(new KeyedCodec<>("@Enabled", Codec.BOOLEAN),
                                (data, value) -> data.enabled = value,
                                data -> data.enabled).add()
                        .append(new KeyedCodec<>("@ItemId", Codec.STRING),
                                (data, value) -> data.itemId = value != null ? value : "",
                                data -> data.itemId).add()
                        .append(new KeyedCodec<>("@Quantity", Codec.DOUBLE),
                                (data, value) -> data.quantity = value,
                                data -> data.quantity).add()
                        .build();

        private String action = "";
        private String kit = "";
        private String name = "";
        private String displayName = "";
        private String description = "";
        private String permission = "";
        private double cooldown;
        private boolean enabled;
        private String itemId = "";
        private double quantity = 1.0D;
    }
}
