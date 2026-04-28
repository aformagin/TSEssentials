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
import com.thirdspare.utils.Teleportation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class HomeCommand extends AbstractCommand {
    private final TSEssentials plugin;
    private final OptionalArg<String> homeNameArg;

    public HomeCommand(@Nullable String name, @Nullable String description, TSEssentials plugin) {
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

        // Get optional home name from command arguments
        String homeName = commandContext.get(homeNameArg);

        // Read home location from the player's persistent ECS component.
        PlayerHomeData homeData = plugin.getHomeService().getHome(playerRef, homeName);

        if (homeData == null) {
            // No home set - send error notification
            String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " '" + homeName + "'" : "";
            CommandUtils.sendNotification(playerRef, "No Home Set!", "#FF0000",
                    "Home" + homeNameDisplay + " not found. Use /sethome" + homeNameDisplay + " to set it.", "#FF6B6B",
                    StaticVariables.HOME_ICON);
            return CompletableFuture.completedFuture(null);
        }

        //Teleport the player to the home location
        Teleportation.teleportPlayer(
                playerRef,
                homeData.getX(), homeData.getY(), homeData.getZ(),
                homeData.getPitch(), homeData.getYaw(), homeData.getRoll(),
                homeData.getWorldUUID()
        );

        // Send success notification
        String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " to '" + homeName + "'" : "";
        CommandUtils.sendNotification(playerRef, "Teleporting!", "#00FF00",
                "Welcome home" + homeNameDisplay + "!", "#228B22",
                StaticVariables.HOME_ICON);

        return CompletableFuture.completedFuture(null);
    }
}
