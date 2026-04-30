package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.core.rules.RulesManager;
import com.thirdspare.core.rules.ui.RulesViewPage;
import com.thirdspare.core.ui.CorePageOpener;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class RulesCommand extends AbstractCommand {
    private final RulesManager manager;

    public RulesCommand(RulesManager manager) {
        super("rules", "View server rules");
        requirePermission(TSEssentialsPermissions.RULES);
        this.manager = manager;
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, true);
        if (player != null) {
            CorePageOpener.open(player, new RulesViewPage(player, manager));
            manager.sendTo(player);
        }
        return CompletableFuture.completedFuture(null);
    }
}
