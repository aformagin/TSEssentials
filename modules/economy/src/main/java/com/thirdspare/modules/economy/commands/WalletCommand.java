package com.thirdspare.modules.economy.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.modules.economy.EconomyService;
import com.thirdspare.modules.economy.ui.EconomyPage;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class WalletCommand extends AbstractCommand {
    private final EconomyService economyService;
    private final TSEUiDocument economyUi;

    public WalletCommand(EconomyService economyService, TSEUiDocument economyUi) {
        super("wallet", "Open your economy wallet");
        requirePermission(TSEssentialsPermissions.ECONOMY_UI);
        this.economyService = economyService;
        this.economyUi = economyUi;
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
                    playerComponent.getPageManager().openCustomPage(ref, store,
                            new EconomyPage(player, economyService, economyUi));
                }
            } catch (IllegalStateException ignored) {
                // The player can leave before the queued UI open runs.
            }
        });
    }
}
