package com.thirdspare.modules.claims.data;

import java.util.UUID;

public record ClaimLookupKey(UUID worldUuid, int chunkX, int chunkZ) {

    public static ClaimLookupKey of(UUID worldUuid, int x, int z) {
        return new ClaimLookupKey(worldUuid, x >> 4, z >> 4);
    }
}
