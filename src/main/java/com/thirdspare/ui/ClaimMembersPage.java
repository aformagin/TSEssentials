package com.thirdspare.ui;

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
import com.thirdspare.claims.ClaimManager;
import com.thirdspare.data.claims.Claim;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ClaimMembersPage extends InteractiveCustomUIPage<ClaimMembersPage.ClaimMembersEventData> {
    private static final int MAX_VISIBLE_CLAIM_ROWS = 10;
    private static final int MAX_VISIBLE_MEMBER_ROWS = 10;

    private final ClaimManager claimManager;
    private final PlayerRef viewer;
    private String selectedClaimId;
    private String selectedMemberUuid;
    private String statusMessage = "";
    private boolean errorStatus;

    public ClaimMembersPage(PlayerRef playerRef, ClaimManager claimManager, String selectedClaimId) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClaimMembersEventData.CODEC);
        this.viewer = playerRef;
        this.claimManager = claimManager;
        this.selectedClaimId = selectedClaimId;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append("ClaimMembers.ui");
        ensureSelection();
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                ClaimMembersEventData data) {
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        switch (data.action) {
            case "SelectClaim" -> selectedClaimId = data.claimId;
            case "SelectMember" -> selectedMemberUuid = data.memberUuid;
            case "AddTrusted" -> addTrusted(data.playerName);
            case "RemoveTrusted" -> removeTrusted();
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
        List<Claim> claims = claimManager.getManageableClaims(viewer);
        for (int i = 0; i < MAX_VISIBLE_CLAIM_ROWS; i++) {
            String rowId = "#ClaimRow" + i;
            if (i < claims.size()) {
                Claim claim = claims.get(i);
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", claimRowLabel(claim));
                events.addEventBinding(CustomUIEventBindingType.Activating, rowId,
                        new EventData()
                                .append("Action", "SelectClaim")
                                .append("ClaimId", claim.getId()),
                        false);
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }

        events.addEventBinding(CustomUIEventBindingType.Activating, "#AddButton", addEventData(), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RemoveButton",
                EventData.of("Action", "RemoveTrusted"), false);

        renderSelected(builder, events);
        renderStatus(builder);
    }

    private void renderSelected(UICommandBuilder builder, UIEventBuilder events) {
        Claim selected = selectedClaim();
        if (selected == null) {
            builder.set("#SelectedTitle.Text", "No claim selected");
            builder.set("#OwnerText.Text", "");
            builder.set("#BoundsText.Text", "");
            builder.set("#PlayerInput.Value", "");
            renderMemberRows(builder, events, List.of());
            return;
        }

        builder.set("#SelectedTitle.Text", selected.getName());
        builder.set("#OwnerText.Text", "Owner: " + selected.getOwnerName());
        builder.set("#BoundsText.Text", "Bounds: " + selected.getRegion().describeBounds());
        renderMemberRows(builder, events, selected.getTrustedUuids().stream()
                .sorted(Comparator.comparing(selected::displayMember))
                .toList());
    }

    private void renderMemberRows(UICommandBuilder builder, UIEventBuilder events, List<UUID> members) {
        for (int i = 0; i < MAX_VISIBLE_MEMBER_ROWS; i++) {
            String rowId = "#MemberRow" + i;
            if (i < members.size()) {
                UUID uuid = members.get(i);
                Claim selected = selectedClaim();
                String label = (uuid.toString().equals(selectedMemberUuid) ? "> " : "  ")
                        + (selected != null ? selected.displayMember(uuid) : uuid.toString());
                builder.set(rowId + ".Visible", true);
                builder.set(rowId + ".Text", label);
                events.addEventBinding(CustomUIEventBindingType.Activating, rowId,
                        new EventData()
                                .append("Action", "SelectMember")
                                .append("MemberUuid", uuid.toString()),
                        false);
            } else {
                builder.set(rowId + ".Visible", false);
                builder.set(rowId + ".Text", "");
            }
        }
    }

    private String claimRowLabel(Claim claim) {
        String selectedMarker = claim.getId().equals(selectedClaimId) ? "> " : "  ";
        return selectedMarker + claim.getName() + " (" + claim.getTrustedUuids().size() + ")";
    }

    private EventData addEventData() {
        return new EventData()
                .append("Action", "AddTrusted")
                .append("@PlayerName", "#PlayerInput.Value");
    }

    private void addTrusted(String playerName) {
        Claim claim = selectedClaim();
        if (claim == null) {
            setStatus("Select a claim first.", null);
            return;
        }
        PlayerRef target = PlayerLookup.findPlayerByName(playerName).orElse(null);
        if (target == null) {
            setStatus("Player must be online to trust them.", null);
            return;
        }
        String error = claimManager.trust(viewer, claim, target);
        setStatus(error, "Trusted " + target.getUsername() + ".");
    }

    private void removeTrusted() {
        Claim claim = selectedClaim();
        if (claim == null) {
            setStatus("Select a claim first.", null);
            return;
        }
        if (selectedMemberUuid == null || selectedMemberUuid.isBlank()) {
            setStatus("Select a member first.", null);
            return;
        }
        try {
            UUID uuid = UUID.fromString(selectedMemberUuid);
            String error = claimManager.untrust(viewer, claim, uuid);
            if (error == null) {
                selectedMemberUuid = null;
            }
            setStatus(error, "Removed trusted member.");
        } catch (IllegalArgumentException ex) {
            setStatus("Invalid member selection.", null);
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

    private void ensureSelection() {
        if (selectedClaim() != null) {
            return;
        }
        List<Claim> claims = claimManager.getManageableClaims(viewer);
        selectedClaimId = claims.isEmpty() ? null : claims.getFirst().getId();
        selectedMemberUuid = null;
    }

    private Claim selectedClaim() {
        if (selectedClaimId == null || selectedClaimId.isBlank()) {
            return null;
        }
        Claim claim = claimManager.getClaims().stream()
                .filter(candidate -> candidate.getId().equals(selectedClaimId))
                .findFirst()
                .orElse(null);
        return claimManager.canManage(viewer, claim) ? claim : null;
    }

    public static class ClaimMembersEventData {
        public static final BuilderCodec<ClaimMembersEventData> CODEC =
                BuilderCodec.builder(ClaimMembersEventData.class, ClaimMembersEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value != null ? value : "",
                                data -> data.action).add()
                        .append(new KeyedCodec<>("ClaimId", Codec.STRING),
                                (data, value) -> data.claimId = value != null ? value : "",
                                data -> data.claimId).add()
                        .append(new KeyedCodec<>("MemberUuid", Codec.STRING),
                                (data, value) -> data.memberUuid = value != null ? value : "",
                                data -> data.memberUuid).add()
                        .append(new KeyedCodec<>("@PlayerName", Codec.STRING),
                                (data, value) -> data.playerName = value != null ? value : "",
                                data -> data.playerName).add()
                        .build();

        private String action = "";
        private String claimId = "";
        private String memberUuid = "";
        private String playerName = "";
    }
}
