package com.thirdspare.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.PlayerHomeData;
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
        var playerUUID = playerRef.getUuid();

        // Get optional home name from command arguments
        String homeName = commandContext.get(homeNameArg);

        // Check if player has reached max homes limit (only if setting a new home)
        if (!plugin.getPlayerData().hasHome(playerUUID, homeName)) {
            int currentHomes = plugin.getPlayerData().getHomeCount(playerUUID);
            int maxHomes = plugin.getPlayerData().getMaxHomes();

            if (currentHomes >= maxHomes) {
                // Send error notification - home limit reached
                CommandUtils.sendNotification(playerRef, "Home Limit Reached!", "#FF0000",
                        "You can only have " + maxHomes + " home(s).", "#FF6B6B",
                        StaticVariables.HOME_ICON);
                return CompletableFuture.completedFuture(null);
            }
        }

        //Get the current location of the player
        var worldUUID = playerRef.getWorldUuid();

        //These are needed to be saved
        var playerTransformPosition = playerRef.getTransform().getPosition();
        var playerTransformRotation = playerRef.getTransform().getRotation();

        //UUID null should not be possible
        if (worldUUID == null) return CompletableFuture.completedFuture(null);

        //Save current location to data file
        PlayerHomeData homeData = new PlayerHomeData(
                worldUUID.toString(),
                playerTransformPosition.x(),
                playerTransformPosition.y(),
                playerTransformPosition.z(),
                playerTransformRotation.pitch(),
                playerTransformRotation.yaw(),
                playerTransformRotation.roll()
        );

        plugin.getPlayerData().setHome(playerUUID, homeName, homeData);
        plugin.savePlayerData();

        //Send the notification
        String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " '" + homeName + "'" : "";
        CommandUtils.sendNotification(playerRef, "Success!", "#00FF00",
                "Your home" + homeNameDisplay + " has been set.", "#228B22",
                StaticVariables.HOME_ICON);

        return CompletableFuture.completedFuture(null);
    }
}
