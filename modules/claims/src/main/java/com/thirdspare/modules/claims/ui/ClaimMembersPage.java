package com.thirdspare.modules.claims.ui;

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
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.thirdspare.utils.PlayerLookup;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ClaimMembersPage extends InteractiveCustomUIPage<ClaimMembersPage.ClaimMembersEventData> {
    private static final int MAX_VISIBLE_CLAIM_ROWS = 10;
    private static final int MAX_VISIBLE_MEMBER_ROWS = 10;

    private final ClaimsService claimsService;
    private final PlayerRef viewer;
    private final TSEUiDocument uiDocument;
    private String selectedClaimId;
    private String selectedMemberUuid;
    private String statusMessage = "";
    private boolean errorStatus;

    public ClaimMembersPage(PlayerRef playerRef, ClaimsService claimsService,
                            TSEUiDocument uiDocument, String selectedClaimId) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClaimMembersEventData.CODEC);
        this.viewer = playerRef;
        this.claimsService = claimsService;
        this.uiDocument = uiDocument;
        this.selectedClaimId = selectedClaimId;
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
                                ClaimMembersEventData data) {
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        switch (data.action) {
            case "SelectClaim" -> { selectedClaimId = data.claimId; statusMessage = ""; }
            case "SelectMember" -> { selectedMemberUuid = data.memberUuid; statusMessage = ""; }
            case "AddTrusted" -> addTrusted(data.playerName);
            case "RemoveTrusted" -> removeTrusted();
            default -> { statusMessage = ""; errorStatus = false; }
        }

        ensureSelection();
        render(builder, events);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder, UIEventBuilder events) {
        List<ClaimDefinition> claims = claimsService.getClaimsFor(viewer);
        for (int i = 0; i < MAX_VISIBLE_CLAIM_ROWS; i++) {
            String rowId = "#ClaimRow" + i;
            if (i < claims.size()) {
                ClaimDefinition claim = claims.get(i);
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
        ClaimDefinition selected = selectedClaim();
        if (selected == null) {
            builder.set("#SelectedTitle.Text", "No claim selected");
            builder.set("#OwnerText.Text", "");
            builder.set("#BoundsText.Text", "");
            builder.set("#PlayerInput.Value", "");
            renderMemberRows(builder, events, List.of(), null);
            return;
        }
        builder.set("#SelectedTitle.Text", selected.getDisplayName());
        builder.set("#OwnerText.Text", "Owner: " + selected.getOwnerName());
        builder.set("#BoundsText.Text", "Bounds: " + selected.getBounds().describeBounds());
        List<UUID> members = selected.getTrustedUuids().stream()
                .sorted(Comparator.comparing(selected::displayMember))
                .toList();
        renderMemberRows(builder, events, members, selected);
    }

    private void renderMemberRows(UICommandBuilder builder, UIEventBuilder events,
                                  List<UUID> members, ClaimDefinition selected) {
        for (int i = 0; i < MAX_VISIBLE_MEMBER_ROWS; i++) {
            String rowId = "#MemberRow" + i;
            if (i < members.size()) {
                UUID uuid = members.get(i);
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

    private String claimRowLabel(ClaimDefinition claim) {
        String marker = claim.getId().equals(selectedClaimId) ? "> " : "  ";
        return marker + claim.getDisplayName() + " (" + claim.getTrustedUuids().size() + ")";
    }

    private EventData addEventData() {
        return new EventData()
                .append("Action", "AddTrusted")
                .append("@PlayerName", "#PlayerInput.Value");
    }

    private void addTrusted(String playerName) {
        ClaimDefinition claim = selectedClaim();
        if (claim == null) { setStatus("Select a claim first.", null); return; }
        PlayerRef target = PlayerLookup.findPlayerByName(playerName).orElse(null);
        if (target == null) { setStatus("Player must be online to trust them.", null); return; }
        String error = claimsService.trust(viewer, claim.getId(), target.getUuid(), target.getUsername());
        setStatus(error, "Trusted " + target.getUsername() + ".");
    }

    private void removeTrusted() {
        ClaimDefinition claim = selectedClaim();
        if (claim == null) { setStatus("Select a claim first.", null); return; }
        if (selectedMemberUuid == null || selectedMemberUuid.isBlank()) {
            setStatus("Select a member first.", null);
            return;
        }
        try {
            UUID uuid = UUID.fromString(selectedMemberUuid);
            String error = claimsService.untrust(viewer, claim.getId(), uuid);
            if (error == null) selectedMemberUuid = null;
            setStatus(error, "Removed trusted member.");
        } catch (IllegalArgumentException ex) {
            setStatus("Invalid member selection.", null);
        }
    }

    private void renderStatus(UICommandBuilder builder) {
        boolean visible = statusMessage != null && !statusMessage.isBlank();
        builder.set("#StatusMessage.Visible", visible);
        builder.set("#StatusMessage.Text", visible ? statusMessage : "");
        builder.set("#StatusMessage.Style.TextColor", errorStatus ? "#FFB4B4" : "#BDF2C3");
    }

    private void setStatus(String error, String success) {
        errorStatus = error != null;
        statusMessage = error != null ? error : success;
    }

    private void ensureSelection() {
        if (selectedClaim() != null) return;
        List<ClaimDefinition> claims = claimsService.getClaimsFor(viewer);
        selectedClaimId = claims.isEmpty() ? null : claims.getFirst().getId();
        selectedMemberUuid = null;
    }

    private ClaimDefinition selectedClaim() {
        if (selectedClaimId == null || selectedClaimId.isBlank()) return null;
        return claimsService.getClaimsFor(viewer).stream()
                .filter(c -> c.getId().equals(selectedClaimId))
                .findFirst()
                .orElse(null);
    }

    public static class ClaimMembersEventData {
        public static final BuilderCodec<ClaimMembersEventData> CODEC =
                BuilderCodec.builder(ClaimMembersEventData.class, ClaimMembersEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (d, v) -> d.action = v != null ? v : "",
                                d -> d.action).add()
                        .append(new KeyedCodec<>("ClaimId", Codec.STRING),
                                (d, v) -> d.claimId = v != null ? v : "",
                                d -> d.claimId).add()
                        .append(new KeyedCodec<>("MemberUuid", Codec.STRING),
                                (d, v) -> d.memberUuid = v != null ? v : "",
                                d -> d.memberUuid).add()
                        .append(new KeyedCodec<>("@PlayerName", Codec.STRING),
                                (d, v) -> d.playerName = v != null ? v : "",
                                d -> d.playerName).add()
                        .build();

        private String action = "";
        private String claimId = "";
        private String memberUuid = "";
        private String playerName = "";
    }
}
