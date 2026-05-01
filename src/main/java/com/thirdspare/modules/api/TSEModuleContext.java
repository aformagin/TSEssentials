package com.thirdspare.modules.api;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.TSEssentials;

import java.nio.file.Path;

public interface TSEModuleContext {
    TSEssentials core();

    HytaleLogger logger();

    Path moduleJarPath();

    Path moduleDataDirectory();

    <T> Config<T> registerConfig(String key, BuilderCodec<T> codec);

    <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerComponent(
            Class<T> type,
            String componentId,
            BuilderCodec<T> codec
    );

    TSEUiDocument registerUiDocument(String documentName, String resourcePath);

    void registerCommand(AbstractCommand command);

    EventRegistry eventRegistry();
}
