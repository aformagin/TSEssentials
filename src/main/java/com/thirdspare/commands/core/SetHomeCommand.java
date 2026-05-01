package com.thirdspare.commands.core;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.TSEssentials;
import com.thirdspare.core.homes.data.PlayerHomeData;
import com.thirdspare.permissions.TSEssentialsPermissions;
import com.thirdspare.utils.CommandUtils;
import com.thirdspare.utils.StaticVariables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SetHomeCommand extends AbstractCommand {
    private final TSEssentials plugin;
    private final OptionalArg<String> homeNameArg;

    public SetHomeCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
        super(name, description);
        requirePermission(TSEssentialsPermissions.SET_HOME);
        this.plugin = plugin;
        this.homeNameArg = withOptionalArg("home-name", "Name of the home", ArgTypes.STRING);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext commandContext) {
        PlayerRef playerRef = CommandUtils.getPlayerFromContext(commandContext, true);
        if (playerRef == null) {
            return CompletableFuture.completedFuture(null);
        }
        // Get optional home name from command arguments
        String homeName = commandContext.get(homeNameArg);

        CommandUtils.runOnPlayerWorld(commandContext, playerRef, scheduledPlayer -> {
            // Check if player has reached max homes limit (only if setting a new home)
            if (!plugin.getHomeService().hasHome(scheduledPlayer, homeName)) {
                int currentHomes = plugin.getHomeService().getHomeCount(scheduledPlayer);
                int maxHomes = plugin.getHomeService().getMaxHomes();

                if (currentHomes >= maxHomes) {
                    // Send error notification - home limit reached
                    CommandUtils.sendNotification(scheduledPlayer, "Home Limit Reached!", "#FF0000",
                            "You can only have " + maxHomes + " home(s).", "#FF6B6B",
                            StaticVariables.HOME_ICON);
                    return;
                }
            }

            // Get the current location of the player
            var worldUUID = scheduledPlayer.getWorldUuid();

            // These are needed to be saved
            var playerTransformPosition = scheduledPlayer.getTransform().getPosition();
            var playerTransformRotation = scheduledPlayer.getTransform().getRotation();

            // UUID null should not be possible
            if (worldUUID == null) return;

            // Save current location to the player's persistent ECS component.
            PlayerHomeData homeData = new PlayerHomeData(
                    worldUUID.toString(),
                    playerTransformPosition.getX(),
                    playerTransformPosition.getY(),
                    playerTransformPosition.getZ(),
                    playerTransformRotation.getPitch(),
                    playerTransformRotation.getYaw(),
                    playerTransformRotation.getRoll()
            );

            plugin.getHomeService().setHome(scheduledPlayer, homeName, homeData);

            // Send the notification
            String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " '" + homeName + "'" : "";
            CommandUtils.sendNotification(scheduledPlayer, "Success!", "#00FF00",
                    "Your home" + homeNameDisplay + " has been set.", "#228B22",
                    StaticVariables.HOME_ICON);
        });

        return CompletableFuture.completedFuture(null);
    }
}
