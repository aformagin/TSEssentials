package com.thirdspare.commands;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.thirdspare.TSEssentials;
import com.thirdspare.data.PlayerHomeData;
import com.thirdspare.utils.StaticVariables;
import com.thirdspare.utils.Teleportation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
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
        if (!commandContext.isPlayer()) {
            commandContext.sendMessage(Message.raw("This command can only be used by players!").color("#FF0000"));
            return CompletableFuture.completedFuture(null);
        }

        var playerUUID = commandContext.sender().getUuid();
        var playerRef = Universe.get().getPlayer(playerUUID);

        if (playerRef == null) {
            commandContext.sendMessage(Message.raw("Unable to find player!").color("#FF0000"));
            return CompletableFuture.completedFuture(null);
        }

        // Get optional home name from command arguments
        String homeName = commandContext.get(homeNameArg);

        //Read home location from player_data
        PlayerHomeData homeData = plugin.getPlayerData().getHome(playerUUID, homeName);

        if (homeData == null) {
            // No home set - send error notification
            var packetHandler = playerRef.getPacketHandler();
            if (packetHandler != null) {
                var primaryMessage = Message.raw("No Home Set!").color("#FF0000");
                String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " '" + homeName + "'" : "";
                var secondaryMessage = Message.raw("Home" + homeNameDisplay + " not found. Use /sethome" + homeNameDisplay + " to set it.").color("#FF6B6B");
                var icon = new ItemStack(StaticVariables.HOME_ICON, 1).toPacket();

                NotificationUtil.sendNotification(
                        packetHandler,
                        primaryMessage,
                        secondaryMessage,
                        (ItemWithAllMetadata) icon);
            }
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
        var packetHandler = playerRef.getPacketHandler();
        if (packetHandler != null) {
            var primaryMessage = Message.raw("Teleporting!").color("#00FF00");
            String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " to '" + homeName + "'" : "";
            var secondaryMessage = Message.raw("Welcome home" + homeNameDisplay + "!").color("#228B22");
            var icon = new ItemStack(StaticVariables.HOME_ICON, 1).toPacket();

            NotificationUtil.sendNotification(
                    packetHandler,
                    primaryMessage,
                    secondaryMessage,
                    (ItemWithAllMetadata) icon);
        }

        return CompletableFuture.completedFuture(null);
    }
}
