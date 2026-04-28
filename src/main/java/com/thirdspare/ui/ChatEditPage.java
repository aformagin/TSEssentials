package com.thirdspare.ui;

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
import com.thirdspare.chat.ChannelManager;
import com.thirdspare.data.chat.ChatChannel;

import javax.annotation.Nonnull;
import java.util.List;

public class ChatEditPage extends InteractiveCustomUIPage<ChatEditPage.ChatEditEventData> {
    private static final int MAX_VISIBLE_CHANNEL_ROWS = 10;
    private static final String[][] COLOR_PRESETS = {
            {"#E6E6E6", "Gray"},
            {"#8DE969", "Green"},
            {"#FFB347", "Gold"},
            {"#008080", "Teal"},
            {"#7AB7FF", "Blue"},
            {"#FF6B6B", "Red"},
            {"#FF8AE2", "Pink"},
            {"#FFFFFF", "White"}
    };

    private final ChannelManager channelManager;
    private String selectedChannelName;
    private String statusMessage = "";
    private boolean errorStatus;

    public ChatEditPage(PlayerRef playerRef, ChannelManager channelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, ChatEditEventData.CODEC);
        this.channelManager = channelManager;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("ChatEdit.ui");
        ensureSelection();
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                ChatEditEventData data) {
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        switch (data.action) {
            case "Select" -> selectedChannelName = ChatChannel.normalizeName(data.channel);
            case "Add" -> addChannel();
            case "Save" -> saveChannel(data);
            case "Delete" -> deleteSelected();
            default -> {
                statusMessage = "";
                errorStatus = false;
            }
        }

        ensureSelection();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        List<ChatChannel> channels = channelManager.getChannels();

        for (int i = 0; i < MAX_VISIBLE_CHANNEL_ROWS; i++) {
            String rowId = "#ChannelRow" + i;
            if (i < channels.size()) {
                ChatChannel channel = channels.get(i);
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", rowLabel(channel));
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        rowId,
                        new EventData()
                                .append("Action", "Select")
                                .append("Channel", channel.getName()),
                        false
                );
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }

        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddButton",
                EventData.of("Action", "Add"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", saveEventData(), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#DeleteButton",
                EventData.of("Action", "Delete"), false);
        renderColorDropdown(builder);

        renderSelected(builder);
        renderStatus(builder);
    }

    private void renderColorDropdown(UICommandBuilder builder) {
        List<DropdownEntryInfo> entries = new java.util.ArrayList<>();
        for (String[] preset : COLOR_PRESETS) {
            entries.add(new DropdownEntryInfo(
                    LocalizableString.fromString(preset[1] + " (" + preset[0] + ")"),
                    preset[0]
            ));
        }
        builder.set("#ColorDropdown.Entries", entries);
    }

    private void renderSelected(UICommandBuilder builder) {
        ChatChannel selected = channelManager.getChannel(selectedChannelName);
        if (selected == null) {
            builder.set("#SelectedTitle.Text", "No channel selected");
            builder.set("#NameInput.Value", "");
            builder.set("#PrefixInput.Value", "");
            builder.set("#ColorDropdown.Value", "#FFFFFF");
            builder.set("#PermissionInput.Value", "");
            builder.set("#RangeInput.Value", 0.0D);
            builder.set("#RangedCheck.Value", false);
            builder.set("#DefaultSubCheck.Value", false);
            return;
        }

        builder.set("#SelectedTitle.Text", selected.getName());
        builder.set("#NameInput.Value", selected.getName());
        builder.set("#PrefixInput.Value", selected.getPrefix());
        builder.set("#ColorDropdown.Value", normalizePresetColor(selected.getColor()));
        builder.set("#PermissionInput.Value", selected.getPermission());
        builder.set("#RangeInput.Value", selected.getRange());
        builder.set("#RangedCheck.Value", selected.isRanged());
        builder.set("#DefaultSubCheck.Value", selected.isDefaultSubscribed());
    }

    private void renderStatus(UICommandBuilder builder) {
        builder.set("#StatusMessage.Visible", statusMessage != null && !statusMessage.isBlank());
        builder.set("#StatusMessage.Text", statusMessage);
        builder.set("#StatusMessage.Style.TextColor", errorStatus ? "#FFB4B4" : "#BDF2C3");
    }

    private String rowLabel(ChatChannel channel) {
        String selectedMarker = channel.getName().equals(selectedChannelName) ? "> " : "  ";
        String rangeMarker = channel.isRanged() ? " range" : "";
        String permissionMarker = channel.hasPermissionNode() ? " locked" : "";
        return selectedMarker + channel.getName() + " " + channel.getPrefix() + rangeMarker + permissionMarker;
    }

    private EventData saveEventData() {
        return new EventData()
                .append("Action", "Save")
                .append("@Name", "#NameInput.Value")
                .append("@Prefix", "#PrefixInput.Value")
                .append("@Color", "#ColorDropdown.Value")
                .append("@Permission", "#PermissionInput.Value")
                .append("@Ranged", "#RangedCheck.Value")
                .append("@Range", "#RangeInput.Value")
                .append("@DefaultSubscribed", "#DefaultSubCheck.Value");
    }

    private void addChannel() {
        int suffix = 1;
        String name;
        do {
            name = "channel" + suffix++;
        } while (channelManager.hasChannel(name));

        String error = channelManager.createOrUpdate(name, "[C]", "#FFFFFF", false, 0.0D, "", false);
        selectedChannelName = name;
        setStatus(error, "Created channel " + name + ".");
    }

    private void saveChannel(ChatEditEventData data) {
        ChatChannel current = channelManager.getChannel(selectedChannelName);
        if (current == null) {
            setStatus("Select a channel before saving.", null);
            return;
        }

        String newName = ChatChannel.normalizeName(data.name);
        if (ChannelManager.GLOBAL.equals(selectedChannelName) && !ChannelManager.GLOBAL.equals(newName)) {
            setStatus("The global channel cannot be renamed.", null);
            return;
        }

        String error = channelManager.createOrUpdate(
                newName,
                data.prefix,
                data.color,
                data.ranged,
                data.ranged ? data.range : 0.0D,
                data.permission,
                data.defaultSubscribed
        );
        if (error == null && !selectedChannelName.equals(newName)) {
            error = channelManager.delete(selectedChannelName);
        }

        if (error == null) {
            selectedChannelName = newName;
        }
        setStatus(error, "Saved channel " + selectedChannelName + ".");
    }

    private void deleteSelected() {
        String deletedName = selectedChannelName;
        String error = channelManager.delete(deletedName);
        if (error == null) {
            selectedChannelName = null;
        }
        setStatus(error, "Deleted channel " + deletedName + ".");
    }

    private void setStatus(String error, String success) {
        errorStatus = error != null;
        statusMessage = error != null ? error : success;
    }

    private void ensureSelection() {
        if (selectedChannelName != null && channelManager.hasChannel(selectedChannelName)) {
            return;
        }
        ChatChannel global = channelManager.getGlobalChannel();
        if (global != null) {
            selectedChannelName = global.getName();
            return;
        }
        List<ChatChannel> channels = channelManager.getChannels();
        selectedChannelName = channels.isEmpty() ? null : channels.getFirst().getName();
    }

    private String normalizePresetColor(String color) {
        if (color == null) {
            return "#FFFFFF";
        }
        for (String[] preset : COLOR_PRESETS) {
            if (preset[0].equalsIgnoreCase(color)) {
                return preset[0];
            }
        }
        return "#FFFFFF";
    }

    public static class ChatEditEventData {
        public static final BuilderCodec<ChatEditEventData> CODEC =
                BuilderCodec.builder(ChatEditEventData.class, ChatEditEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("Channel", Codec.STRING),
                                (data, value) -> data.channel = value != null ? value : "",
                                data -> data.channel).add()
                        .append(new KeyedCodec<>("@Name", Codec.STRING),
                                (data, value) -> data.name = value != null ? value : "",
                                data -> data.name).add()
                        .append(new KeyedCodec<>("@Prefix", Codec.STRING),
                                (data, value) -> data.prefix = value != null ? value : "",
                                data -> data.prefix).add()
                        .append(new KeyedCodec<>("@Color", Codec.STRING),
                                (data, value) -> data.color = value != null ? value : "#FFFFFF",
                                data -> data.color).add()
                        .append(new KeyedCodec<>("@Permission", Codec.STRING),
                                (data, value) -> data.permission = value != null ? value : "",
                                data -> data.permission).add()
                        .append(new KeyedCodec<>("@Ranged", Codec.BOOLEAN),
                                (data, value) -> data.ranged = value,
                                data -> data.ranged).add()
                        .append(new KeyedCodec<>("@Range", Codec.DOUBLE),
                                (data, value) -> data.range = value,
                                data -> data.range).add()
                        .append(new KeyedCodec<>("@DefaultSubscribed", Codec.BOOLEAN),
                                (data, value) -> data.defaultSubscribed = value,
                                data -> data.defaultSubscribed).add()
                        .build();

        private String action = "";
        private String channel = "";
        private String name = "";
        private String prefix = "";
        private String color = "#FFFFFF";
        private String permission = "";
        private boolean ranged;
        private double range;
        private boolean defaultSubscribed;
    }
}
