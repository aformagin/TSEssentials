package com.thirdspare.modules.claims.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.claims.ClaimSelectionService;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;

import javax.annotation.Nonnull;
import java.util.List;

public class ClaimsAdminPage extends InteractiveCustomUIPage<ClaimsAdminPage.ClaimsAdminEventData> {
    private static final int MAX_VISIBLE_CLAIM_ROWS = 10;

    private final ClaimsService claimsService;
    private final PlayerRef viewer;
    private final TSEUiDocument adminUiDocument;
    private final TSEUiDocument membersUiDocument;
    private String selectedClaimId;
    private String statusMessage = "";
    private boolean errorStatus;

    public ClaimsAdminPage(PlayerRef playerRef, ClaimsService claimsService,
                           TSEUiDocument adminUiDocument, TSEUiDocument membersUiDocument) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClaimsAdminEventData.CODEC);
        this.viewer = playerRef;
        this.claimsService = claimsService;
        this.adminUiDocument = adminUiDocument;
        this.membersUiDocument = membersUiDocument;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        adminUiDocument.appendTo(builder);
        ensureSelection();
        render(builder, events);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                ClaimsAdminEventData data) {
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        switch (data.action) {
            case "SelectClaim" -> { selectedClaimId = data.claimId; statusMessage = ""; }
            case "CreateClaim" -> createClaim(data.claimName);
            case "DeleteClaim" -> deleteClaim(ref, store);
            case "OpenMembers" -> {
                sendUpdate(builder, events, false);
                openMembersPage();
                return;
            }
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

        ClaimDefinition selected = selectedClaim();
        boolean hasSelected = selected != null;
        renderSelectionDetail(builder, selected);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateButton",
                new EventData().append("Action", "CreateClaim").append("@ClaimName", "#NameInput.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#DeleteButton",
                EventData.of("Action", "DeleteClaim"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#MembersButton",
                EventData.of("Action", "OpenMembers"), false);

        builder.set("#DeleteButton.Visible", hasSelected && claimsService.canManage(viewer, selected));
        builder.set("#MembersButton.Visible", hasSelected && claimsService.canManage(viewer, selected));

        ClaimSelectionService.Corner pos1 = claimsService.selectionService().getPos1(viewer.getUuid());
        ClaimSelectionService.Corner pos2 = claimsService.selectionService().getPos2(viewer.getUuid());
        String selStatus = (pos1 != null ? "Pos1: " + pos1.x() + "," + pos1.y() + "," + pos1.z() : "Pos1: not set")
                + "  "
                + (pos2 != null ? "Pos2: " + pos2.x() + "," + pos2.y() + "," + pos2.z() : "Pos2: not set");
        builder.set("#SelectionStatus.Text", selStatus);

        renderStatus(builder);
    }

    private void renderSelectionDetail(UICommandBuilder builder, ClaimDefinition selected) {
        if (selected == null) {
            builder.set("#SelectedTitle.Text", "No claim selected");
            builder.set("#SelectedBounds.Text", "");
            return;
        }
        builder.set("#SelectedTitle.Text", selected.getDisplayName());
        builder.set("#SelectedBounds.Text", selected.getBounds().describeBounds()
                + " | Owner: " + selected.getOwnerName()
                + " | Trusted: " + selected.getTrustedUuids().size());
    }

    private void createClaim(String name) {
        if (name == null || name.isBlank()) {
            setStatus("Enter a claim name.", null);
            return;
        }
        String error = claimsService.createClaimFromSelection(viewer, name);
        if (error == null) {
            String normalized = ClaimDefinition.normalizeName(name);
            selectedClaimId = ClaimDefinition.claimId(viewer.getUuid(), normalized);
        }
        setStatus(error, "Created claim '" + ClaimDefinition.normalizeName(name) + "'.");
    }

    private void deleteClaim(Ref<EntityStore> ref, Store<EntityStore> store) {
        ClaimDefinition selected = selectedClaim();
        if (selected == null) { setStatus("No claim selected.", null); return; }
        String error = claimsService.deleteClaim(viewer, selected.getId());
        if (error == null) selectedClaimId = null;
        setStatus(error, "Deleted claim '" + selected.getDisplayName() + "'.");
    }

    private void openMembersPage() {
        World world = Universe.get().getWorld(viewer.getWorldUuid());
        if (world == null) return;
        String claimId = this.selectedClaimId;
        world.execute(() -> {
            if (!viewer.isValid()) return;
            try {
                var ref = viewer.getReference();
                var store = ref.getStore();
                Player pc = store.getComponent(ref, Player.getComponentType());
                if (pc != null) {
                    pc.getPageManager().openCustomPage(ref, store,
                            new ClaimMembersPage(viewer, claimsService, membersUiDocument, claimId));
                }
            } catch (IllegalStateException ignored) {
            }
        });
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
    }

    private String claimRowLabel(ClaimDefinition claim) {
        String marker = claim.getId().equals(selectedClaimId) ? "> " : "  ";
        return marker + claim.getDisplayName() + " (" + claim.getTrustedUuids().size() + ")";
    }

    private ClaimDefinition selectedClaim() {
        if (selectedClaimId == null) return null;
        return claimsService.getClaimsFor(viewer).stream()
                .filter(c -> c.getId().equals(selectedClaimId))
                .findFirst()
                .orElse(null);
    }

    public static class ClaimsAdminEventData {
        public static final BuilderCodec<ClaimsAdminEventData> CODEC =
                BuilderCodec.builder(ClaimsAdminEventData.class, ClaimsAdminEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (d, v) -> d.action = v != null ? v : "",
                                d -> d.action).add()
                        .append(new KeyedCodec<>("ClaimId", Codec.STRING),
                                (d, v) -> d.claimId = v != null ? v : "",
                                d -> d.claimId).add()
                        .append(new KeyedCodec<>("@ClaimName", Codec.STRING),
                                (d, v) -> d.claimName = v != null ? v : "",
                                d -> d.claimName).add()
                        .build();

        private String action = "";
        private String claimId = "";
        private String claimName = "";
    }
}
