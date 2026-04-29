package com.thirdspare.modules.permissions.ui;

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
import com.thirdspare.modules.core.PermissionNodeDescriptor;
import com.thirdspare.modules.permissions.PermissionsManager;
import com.thirdspare.modules.permissions.PermissionsService;
import com.thirdspare.modules.permissions.data.PermissionsGroup;
import com.thirdspare.modules.permissions.data.PermissionsUserRecord;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PermissionsAdminPage extends InteractiveCustomUIPage<PermissionsAdminPage.PermissionsEventData> {
    private static final int MAX_GROUP_ROWS = 10;
    private static final int MAX_NODE_ROWS = 8;
    private static final int MAX_MEMBER_ROWS = 8;

    private final PermissionsService service;
    private String selectedGroupName = "";
    private String selectedMemberTarget = "";
    private String pendingDefaultGroupName = "";
    private String statusMessage = "";
    private boolean errorStatus;

    public PermissionsAdminPage(PlayerRef playerRef, PermissionsService service) {
        super(playerRef, CustomPageLifetime.CanDismiss, PermissionsEventData.CODEC);
        this.service = service;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("PermissionsAdmin.ui");
        ensureSelection();
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                PermissionsEventData data) {
        switch (data.action) {
            case "Select" -> {
                selectedGroupName = data.group;
                selectedMemberTarget = "";
                pendingDefaultGroupName = "";
            }
            case "SelectMember" -> selectedMemberTarget = data.memberTarget;
            case "CreateGroup" -> setStatus(service.createGroup(data.groupName, data.displayName));
            case "DeleteGroup" -> {
                setStatus(service.deleteGroup(selectedGroupName));
                if (!errorStatus) {
                    selectedGroupName = "";
                    selectedMemberTarget = "";
                    pendingDefaultGroupName = "";
                }
            }
            case "AddNode" -> setStatus(service.addNode(selectedGroupName, nodeValue(data)));
            case "RemoveNode" -> setStatus(service.removeNode(selectedGroupName, nodeValue(data)));
            case "AddMember" -> setStatus(service.addUserToGroup(data.memberTarget, selectedGroupName));
            case "RemoveMember" -> {
                setStatus(service.removeUserFromGroup(memberTargetValue(data), selectedGroupName));
                if (!errorStatus) {
                    selectedMemberTarget = "";
                }
            }
            case "SetDefaultGroup" -> setDefaultGroup(data.defaultGroup);
            default -> {
                statusMessage = "";
                errorStatus = false;
                pendingDefaultGroupName = "";
            }
        }

        ensureSelection();
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        renderGroups(builder, events);
        renderKnownNodes(builder);
        renderSelected(builder, events);
        renderStatus(builder);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateGroupButton", new EventData()
                .append("Action", "CreateGroup")
                .append("@GroupName", "#GroupNameInput.Value")
                .append("@DisplayName", "#DisplayNameInput.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#DeleteGroupButton",
                EventData.of("Action", "DeleteGroup"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddNodeButton", nodeEvent("AddNode"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RemoveNodeButton", nodeEvent("RemoveNode"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddMemberButton", memberEvent("AddMember"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RemoveMemberButton", memberEvent("RemoveMember"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#DefaultGroupCheck",
                new EventData().append("Action", "SetDefaultGroup").append("@DefaultGroup", "#DefaultGroupCheck.Value"),
                false);
    }

    private void renderGroups(UICommandBuilder builder, UIEventBuilder events) {
        List<PermissionsGroup> groups = service.listGroups();
        for (int i = 0; i < MAX_GROUP_ROWS; i++) {
            String rowId = "#GroupRow" + i;
            if (i < groups.size()) {
                PermissionsGroup group = groups.get(i);
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", rowLabel(group));
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        rowId,
                        new EventData().append("Action", "Select").append("Group", group.getName()),
                        false
                );
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }
    }

    private void renderKnownNodes(UICommandBuilder builder) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        for (PermissionNodeDescriptor descriptor : service.listKnownNodes()) {
            entries.add(new DropdownEntryInfo(
                    LocalizableString.fromString(descriptor.node()),
                    descriptor.node()
            ));
        }
        builder.set("#KnownNodeDropdown.Entries", entries);
        if (!entries.isEmpty()) {
            builder.set("#KnownNodeDropdown.Value", service.listKnownNodes().getFirst().node());
        }
    }

    private void renderSelected(UICommandBuilder builder, UIEventBuilder events) {
        PermissionsGroup selected = service.getGroup(selectedGroupName).orElse(null);
        if (selected == null) {
            builder.set("#SelectedTitle.Text", "No group selected");
            builder.set("#GroupNameInput.Value", "");
            builder.set("#DisplayNameInput.Value", "");
            builder.set("#DefaultGroupCheck.Value", false);
            selectedMemberTarget = "";
            pendingDefaultGroupName = "";
            clearRows(builder, "NodeRow", MAX_NODE_ROWS);
            clearRows(builder, "MemberRow", MAX_MEMBER_ROWS);
            return;
        }

        builder.set("#SelectedTitle.Text", selected.getName() + (selected.isDefaultGroup() ? " (default)" : ""));
        builder.set("#GroupNameInput.Value", selected.getName());
        builder.set("#DisplayNameInput.Value", selected.getDisplayName());
        builder.set("#DefaultGroupCheck.Value", selected.isDefaultGroup());

        List<String> nodes = new ArrayList<>(selected.getPermissionNodes());
        nodes.sort(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < MAX_NODE_ROWS; i++) {
            String rowId = "#NodeRow" + i;
            if (i < nodes.size()) {
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", nodes.get(i));
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }

        List<PermissionsUserRecord> members = service.membersOf(selected.getName());
        members.sort(Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER));
        if (selectedMemberTarget != null && !selectedMemberTarget.isBlank()
                && members.stream().noneMatch(member -> member.getUuidString().equals(selectedMemberTarget))) {
            selectedMemberTarget = "";
        }
        for (int i = 0; i < MAX_MEMBER_ROWS; i++) {
            String rowId = "#MemberRow" + i;
            if (i < members.size()) {
                PermissionsUserRecord member = members.get(i);
                String memberTarget = member.getUuidString();
                String selectedMarker = memberTarget.equals(selectedMemberTarget) ? "> " : "  ";
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", selectedMarker + displayName(member) + " " + memberTarget);
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        rowId,
                        new EventData().append("Action", "SelectMember").append("MemberTarget", memberTarget),
                        false
                );
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }
    }

    private void clearRows(UICommandBuilder builder, String prefix, int count) {
        for (int i = 0; i < count; i++) {
            builder.set("#" + prefix + i + ".Visible", false);
            builder.set("#" + prefix + i + ".Text", "");
        }
    }

    private void renderStatus(UICommandBuilder builder) {
        builder.set("#StatusMessage.Visible", statusMessage != null && !statusMessage.isBlank());
        builder.set("#StatusMessage.Text", statusMessage);
        builder.set("#StatusMessage.Style.TextColor", errorStatus ? "#FFB4B4" : "#BDF2C3");
    }

    private String rowLabel(PermissionsGroup group) {
        String selectedMarker = group.getName().equals(selectedGroupName) ? "> " : "  ";
        String defaultMarker = group.isDefaultGroup() ? " default" : "";
        return selectedMarker + group.getName() + " (" + group.getPermissionNodes().size() + " nodes)" + defaultMarker;
    }

    private EventData nodeEvent(String action) {
        return new EventData()
                .append("Action", action)
                .append("@Node", "#NodeInput.Value")
                .append("@KnownNode", "#KnownNodeDropdown.Value");
    }

    private EventData memberEvent(String action) {
        return new EventData()
                .append("Action", action)
                .append("@MemberTarget", "#MemberInput.Value");
    }

    private String nodeValue(PermissionsEventData data) {
        return data.node != null && !data.node.isBlank() ? data.node : data.knownNode;
    }

    private String memberTargetValue(PermissionsEventData data) {
        return data.memberTarget != null && !data.memberTarget.isBlank() ? data.memberTarget : selectedMemberTarget;
    }

    private void setDefaultGroup(boolean defaultGroup) {
        PermissionsGroup selected = service.getGroup(selectedGroupName).orElse(null);
        if (selected == null) {
            setStatus(PermissionsManager.MutationResult.error("Select a group before changing the default group."));
            return;
        }
        if (defaultGroup && !selected.isDefaultGroup()) {
            PermissionsGroup currentDefault = service.getDefaultGroup().orElse(null);
            if (currentDefault != null && !currentDefault.getName().equals(selected.getName())
                    && !selected.getName().equals(pendingDefaultGroupName)) {
                pendingDefaultGroupName = selected.getName();
                errorStatus = true;
                statusMessage = currentDefault.getName() + " is already default. Click the checkbox again to replace it.";
                return;
            }
        }
        setStatus(service.setDefaultGroup(selected.getName(), defaultGroup));
        pendingDefaultGroupName = "";
    }

    private void setStatus(PermissionsManager.MutationResult result) {
        errorStatus = !result.success();
        statusMessage = result.message();
    }

    private void ensureSelection() {
        if (selectedGroupName != null && service.getGroup(selectedGroupName).isPresent()) {
            return;
        }
        List<PermissionsGroup> groups = service.listGroups();
        selectedGroupName = groups.isEmpty() ? "" : groups.getFirst().getName();
    }

    private String displayName(PermissionsUserRecord record) {
        String username = record.getLastKnownUsername();
        return username == null || username.isBlank() ? "Unknown" : username;
    }

    public static class PermissionsEventData {
        public static final BuilderCodec<PermissionsEventData> CODEC =
                BuilderCodec.builder(PermissionsEventData.class, PermissionsEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("Group", Codec.STRING),
                                (data, value) -> data.group = value != null ? value : "",
                                data -> data.group).add()
                        .append(new KeyedCodec<>("@GroupName", Codec.STRING),
                                (data, value) -> data.groupName = value != null ? value : "",
                                data -> data.groupName).add()
                        .append(new KeyedCodec<>("@DisplayName", Codec.STRING),
                                (data, value) -> data.displayName = value != null ? value : "",
                                data -> data.displayName).add()
                        .append(new KeyedCodec<>("@Node", Codec.STRING),
                                (data, value) -> data.node = value != null ? value : "",
                                data -> data.node).add()
                        .append(new KeyedCodec<>("@KnownNode", Codec.STRING),
                                (data, value) -> data.knownNode = value != null ? value : "",
                                data -> data.knownNode).add()
                        .append(new KeyedCodec<>("@MemberTarget", Codec.STRING),
                                (data, value) -> data.memberTarget = value != null ? value : "",
                                data -> data.memberTarget).add()
                        .append(new KeyedCodec<>("MemberTarget", Codec.STRING),
                                (data, value) -> {
                                    if (value != null && !value.isBlank()) {
                                        data.memberTarget = value;
                                    }
                                },
                                data -> data.memberTarget).add()
                        .append(new KeyedCodec<>("@DefaultGroup", Codec.BOOLEAN),
                                (data, value) -> data.defaultGroup = value,
                                data -> data.defaultGroup).add()
                        .build();

        private String action = "";
        private String group = "";
        private String groupName = "";
        private String displayName = "";
        private String node = "";
        private String knownNode = "";
        private String memberTarget = "";
        private boolean defaultGroup;
    }
}
