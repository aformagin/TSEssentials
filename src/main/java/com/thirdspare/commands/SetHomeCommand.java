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
        if (commandContext.isPlayer()) {
            var playerUUID = commandContext.sender().getUuid();
            var playerRef = Universe.get().getPlayer(playerUUID);

            if (playerRef == null) {
                commandContext.sendMessage(Message.raw("Unable to find player!").color("#FF0000"));
                return CompletableFuture.completedFuture(null);
            }

            // Get optional home name from command arguments
            String homeName = commandContext.get(homeNameArg);

            // Check if player has reached max homes limit (only if setting a new home)
            if (!plugin.getPlayerData().hasHome(playerUUID, homeName)) {
                int currentHomes = plugin.getPlayerData().getHomeCount(playerUUID);
                int maxHomes = plugin.getPlayerData().getMaxHomes();

                if (currentHomes >= maxHomes) {
                    // Send error notification - home limit reached
                    var packetHandler = playerRef.getPacketHandler();
                    if (packetHandler != null) {
                        var primaryMessage = Message.raw("Home Limit Reached!").color("#FF0000");
                        var secondaryMessage = Message.raw("You can only have " + maxHomes + " home(s).").color("#FF6B6B");
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
                    playerTransformPosition.getX(),
                    playerTransformPosition.getY(),
                    playerTransformPosition.getZ(),
                    playerTransformRotation.getPitch(),
                    playerTransformRotation.getYaw(),
                    playerTransformRotation.getRoll()
            );

            plugin.getPlayerData().setHome(playerUUID, homeName, homeData);
            plugin.savePlayerData();

            //Send the notification
            var packetHandler = playerRef != null ? playerRef.getPacketHandler() : null;
            if (packetHandler != null) {
                var primaryMessage = Message.raw("Success!").color("#00FF00");
                String homeNameDisplay = (homeName != null && !homeName.isEmpty()) ? " '" + homeName + "'" : "";
                var secondaryMessage = Message.raw("Your home" + homeNameDisplay + " has been set.").color("#228B22");
                var icon = new ItemStack(StaticVariables.HOME_ICON, 1).toPacket();

                NotificationUtil.sendNotification(
                        packetHandler,
                        primaryMessage,
                        secondaryMessage,
                        (ItemWithAllMetadata) icon);
            } else {
                commandContext.sendMessage(Message.raw("This command is available only when you are in a player!").color("#FF0000"));
            }

        }

        return CompletableFuture.completedFuture(null);
    }
}
