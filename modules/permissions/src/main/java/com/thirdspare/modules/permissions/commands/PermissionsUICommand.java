package com.thirdspare.modules.permissions.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.thirdspare.modules.permissions.PermissionsService;
import com.thirdspare.modules.permissions.TSEPermissionsNodes;
import com.thirdspare.modules.permissions.ui.PermissionsAdminPage;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class PermissionsUICommand extends AbstractCommand {
    private final PermissionsService service;

    public PermissionsUICommand(PermissionsService service) {
        super("tspermui", "Open permissions admin controls");
        requirePermission(TSEPermissionsNodes.UI);
        this.service = service;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        openPage(context, service);
        return CompletableFuture.completedFuture(null);
    }

    public static void openPage(CommandContext context, PermissionsService service) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player == null) {
            return;
        }
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
                    playerComponent.getPageManager().openCustomPage(ref, store, new PermissionsAdminPage(player, service));
                }
            } catch (IllegalStateException ignored) {
                // The player can leave before the queued UI open runs.
            }
        });
    }
}
