package com.thirdspare.modules.claims.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ClaimViewPage extends InteractiveCustomUIPage<ClaimViewPage.ClaimViewEventData> {
    private final ClaimsService claimsService;
    private final PlayerRef viewer;
    private final TSEUiDocument uiDocument;
    private String claimId;

    public ClaimViewPage(PlayerRef playerRef, ClaimsService claimsService,
                         TSEUiDocument uiDocument, String claimId) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClaimViewEventData.CODEC);
        this.viewer = playerRef;
        this.claimsService = claimsService;
        this.uiDocument = uiDocument;
        this.claimId = claimId;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        uiDocument.appendTo(builder);
        render(builder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                ClaimViewEventData data) {
        UICommandBuilder builder = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        render(builder);
        sendUpdate(builder, events, false);
    }

    private void render(UICommandBuilder builder) {
        Optional<ClaimDefinition> opt = claimId == null ? Optional.empty()
                : claimsService.claimsManager().getClaim(claimId);

        if (opt.isEmpty()) {
            builder.set("#ClaimName.Text", "No claim");
            builder.set("#OwnerText.Text", "");
            builder.set("#BoundsText.Text", "");
            builder.set("#TrustedText.Text", "");
            builder.set("#StatusText.Text", "");
            return;
        }

        ClaimDefinition claim = opt.get();
        builder.set("#ClaimName.Text", claim.getDisplayName());
        builder.set("#OwnerText.Text", "Owner: " + claim.getOwnerName());
        builder.set("#BoundsText.Text", "Bounds: " + claim.getBounds().describeBounds());
        builder.set("#TrustedText.Text", "Trusted players: " + claim.getTrustedUuids().size());

        String status;
        if (claim.isOwner(viewer.getUuid())) {
            status = "You are the owner.";
        } else if (claim.isTrusted(viewer.getUuid())) {
            status = "You are trusted.";
        } else if (claimsService.isBypassing(viewer)) {
            status = "Admin bypass active.";
        } else if (claimsService.isAdmin(viewer)) {
            status = "You have admin access.";
        } else {
            status = "You are not authorized.";
        }
        builder.set("#StatusText.Text", status);
    }

    public static class ClaimViewEventData {
        public static final BuilderCodec<ClaimViewEventData> CODEC =
                BuilderCodec.builder(ClaimViewEventData.class, ClaimViewEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (d, v) -> d.action = v != null ? v : "",
                                d -> d.action).add()
                        .build();
        private String action = "";
    }
}
