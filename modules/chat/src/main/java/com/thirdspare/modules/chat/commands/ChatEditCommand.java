package com.thirdspare.modules.chat.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.modules.chat.ChannelManager;
import com.thirdspare.modules.chat.TSEChatPermissionsNodes;
import com.thirdspare.modules.chat.data.ChatChannel;
import com.thirdspare.modules.chat.ui.ChatEditPage;
import com.thirdspare.modules.api.TSEUiDocument;
import com.thirdspare.utils.CommandUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ChatEditCommand extends AbstractCommand {
    private final ChannelManager channelManager;
    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> channelArg;
    private final OptionalArg<String> valueArg;
    private final OptionalArg<String> value2Arg;
    private final OptionalArg<String> value3Arg;
    private final TSEUiDocument chatEditUi;

    public ChatEditCommand(ChannelManager channelManager, TSEUiDocument chatEditUi) {
        super("chatedit", "Edit chat channels");
        this.channelManager = channelManager;
        this.chatEditUi = chatEditUi;
        requirePermission(TSEChatPermissionsNodes.CHAT_EDIT);
        this.actionArg = withOptionalArg("action", "list, create, delete, prefix, color, range, permission, default", ArgTypes.STRING);
        this.channelArg = withOptionalArg("channel", "Channel name", ArgTypes.STRING);
        this.valueArg = withOptionalArg("value", "Value", ArgTypes.STRING);
        this.value2Arg = withOptionalArg("value2", "Second value", ArgTypes.STRING);
        this.value3Arg = withOptionalArg("value3", "Third value", ArgTypes.GREEDY_STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        PlayerRef player = CommandUtils.getPlayerFromContext(context, false);
        String action = context.get(actionArg);
        if (action == null || action.isBlank() || action.equalsIgnoreCase("list")) {
            sendList(context, player);
            openPage(player);
            return CompletableFuture.completedFuture(null);
        }

        String channel = context.get(channelArg);
        String value = context.get(valueArg);
        String value2 = context.get(value2Arg);
        String value3 = context.get(value3Arg);
        String error = null;

        switch (action.toLowerCase()) {
            case "create" -> error = create(channel, value, value2, value3);
            case "delete" -> error = requireChannel(channel) != null ? requireChannel(channel) : channelManager.delete(channel);
            case "prefix" -> error = requireValue(channel, value) != null ? requireValue(channel, value) : channelManager.setPrefix(channel, value);
            case "color" -> error = requireValue(channel, value) != null ? requireValue(channel, value) : channelManager.setColor(channel, value);
            case "range" -> error = setRange(channel, value);
            case "permission" -> error = requireChannel(channel) != null ? requireChannel(channel) : channelManager.setPermission(channel, value != null ? value : "");
            case "default" -> error = setDefault(channel, value);
            default -> error = "Usage: /chatedit [list|create|delete|prefix|color|range|permission|default]";
        }

        if (error != null) {
            context.sendMessage(Message.raw(error).color("#FF6B6B"));
        } else {
            context.sendMessage(Message.raw("Chat channel updated.").color("#8DE969"));
        }
        return CompletableFuture.completedFuture(null);
    }

    private void sendList(CommandContext context, PlayerRef player) {
        Message message = Message.raw("Chat channels: ").color("#FFFFFF");
        for (ChatChannel channel : channelManager.getChannels()) {
            message.insert(Message.raw(channel.getName() + " ").color(channel.getColor()));
        }
        if (player != null) {
            player.sendMessage(message);
        } else {
            context.sendMessage(message);
        }
    }

    private void openPage(PlayerRef player) {
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
                    playerComponent.getPageManager().openCustomPage(ref, store,
                            new ChatEditPage(player, channelManager, chatEditUi));
                }
            } catch (IllegalStateException ignored) {
                // The player can leave before the queued UI open runs.
            }
        });
    }

    private String create(String channel, String prefix, String color, String rangeValue) {
        String required = requireValue(channel, prefix);
        if (required != null) {
            return "Usage: /chatedit create <channel> <prefix> <color> [range]";
        }
        String resolvedColor = color != null ? color : "#FFFFFF";
        double range = parseDouble(rangeValue, 0.0D);
        return channelManager.createOrUpdate(channel, prefix, resolvedColor, range > 0.0D, range, "", false);
    }

    private String setRange(String channel, String value) {
        String required = requireValue(channel, value);
        if (required != null) {
            return "Usage: /chatedit range <channel> <range|0>";
        }
        try {
            return channelManager.setRange(channel, Double.parseDouble(value));
        } catch (NumberFormatException ex) {
            return "Range must be a number.";
        }
    }

    private String setDefault(String channel, String value) {
        String required = requireValue(channel, value);
        if (required != null) {
            return "Usage: /chatedit default <channel> <true|false>";
        }
        return channelManager.setDefaultSubscribed(channel, Boolean.parseBoolean(value));
    }

    private String requireChannel(String channel) {
        return channel == null || channel.isBlank() ? "Channel name is required." : null;
    }

    private String requireValue(String channel, String value) {
        String channelError = requireChannel(channel);
        if (channelError != null) {
            return channelError;
        }
        return value == null || value.isBlank() ? "Value is required." : null;
    }

    private double parseDouble(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
