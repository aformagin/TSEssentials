package com.thirdspare.modules.api;

import com.hypixel.hytale.server.core.universe.PlayerRef;

public interface TSEModule {
    TSEModuleDescriptor descriptor();

    void register(TSEModuleContext context) throws Exception;

    void enable() throws Exception;

    void disable() throws Exception;

    default void onPlayerReady(PlayerRef player) {
    }
}
