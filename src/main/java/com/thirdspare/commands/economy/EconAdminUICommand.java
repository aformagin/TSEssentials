package com.thirdspare.commands.economy;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.thirdspare.economy.EconomyService;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.ui.EconomyAdminPage;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class EconAdminUICommand extends AbstractCommand {
    private final EconomyService economyService;

    public EconAdminUICommand(EconomyService economyService) {
        super("ecoui", "Open economy admin controls");
        requirePermission(TSEssentialsPermissions.ECO_ADMIN_UI);
        this.economyService = economyService;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player != null) {
            openPage(player);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void openPage(PlayerRef player) {
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                var ref = player.getReference();
                var store = ref.getStore();
                Player playerComponent = store.getComponent(ref, Player.getComponentType());
                if (playerComponent != null) {
                    playerComponent.getPageManager().openCustomPage(ref, store, new EconomyAdminPage(player, economyService));
                }
            } catch (IllegalStateException ignored) {
                // The player can leave before the queued UI open runs.
            }
        });
    }
}
