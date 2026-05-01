package com.thirdspare.modules.core;

import com.hypixel.hytale.server.core.asset.common.CommonAsset;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public final class ModuleUiCommonAsset extends CommonAsset {
    private final byte[] bytes;

    public ModuleUiCommonAsset(String documentName, byte[] bytes) {
        super(documentName, bytes);
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    protected CompletableFuture<byte[]> getBlob0() {
        return CompletableFuture.completedFuture(Arrays.copyOf(bytes, bytes.length));
    }
}
