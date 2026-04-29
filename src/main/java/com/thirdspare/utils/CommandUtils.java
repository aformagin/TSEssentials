package com.thirdspare.utils;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class CommandUtils {

    @Nullable
    public static PlayerRef getPlayerFromContext(CommandContext commandContext, boolean sendError) {
        if (!commandContext.isPlayer()) {
            if (sendError) {
                commandContext.sendMessage(Message.raw("This command can only be used by players!").color("#FF0000"));
            }
            return null;
        }

        UUID playerUUID = commandContext.sender().getUuid();
        PlayerRef playerRef = Universe.get().getPlayer(playerUUID);

        if (playerRef == null) {
            if (sendError) {
                commandContext.sendMessage(Message.raw("Unable to find player!").color("#FF0000"));
            }
        }
        return playerRef;
    }

    public static void sendNotification(PlayerRef player, String primaryMessage, String primaryColor,
                                        String secondaryMessage, String secondaryColor, String icon) {
        var packetHandler = player.getPacketHandler();
        if (packetHandler != null) {
            Message primary = Message.raw(primaryMessage).color(primaryColor);
            Message secondary = Message.raw(secondaryMessage).color(secondaryColor);
            ItemStack itemStack = new ItemStack(icon, 1);

            NotificationUtil.sendNotification(
                    packetHandler,
                    primary,
                    secondary,
                    (ItemWithAllMetadata) itemStack.toPacket()
            );
        }
    }

    public static boolean runOnPlayerWorld(CommandContext commandContext, PlayerRef playerRef, Consumer<PlayerRef> task) {
        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) {
            commandContext.sendMessage(Message.raw("Unable to find your current world.").color("#FF0000"));
            return false;
        }

        World world = Universe.get().getWorld(worldUUID);
        if (world == null) {
            commandContext.sendMessage(Message.raw("Unable to access your current world.").color("#FF0000"));
            return false;
        }

        world.execute(() -> {
            if (!playerRef.isValid()) {
                return;
            }
            task.accept(playerRef);
        });
        return true;
    }

    public static boolean hasPermission(PlayerRef player, String permission) {
        if (player == null || permission == null || permission.isBlank()) {
            return false;
        }

        try {
            return PermissionsModule.get().hasPermission(player.getUuid(), permission);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
