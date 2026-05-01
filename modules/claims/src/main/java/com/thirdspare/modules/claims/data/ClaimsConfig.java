package com.thirdspare.modules.claims.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClaimsConfig {
    private int schemaVersion;
    private Map<String, ClaimDefinition> claims;

    public ClaimsConfig() {
        this.schemaVersion = 1;
        this.claims = new HashMap<>();
    }

    public int getSchemaVersion() { return schemaVersion; }

    public Collection<ClaimDefinition> getAllClaims() {
        return claims.values();
    }

    public ClaimDefinition getClaim(String id) {
        return id == null ? null : claims.get(id);
    }

    public void putClaim(ClaimDefinition claim) {
        if (claim != null && claim.getId() != null && !claim.getId().isBlank()) {
            claims.put(claim.getId(), claim);
        }
    }

    public boolean removeClaim(String id) {
        return id != null && claims.remove(id) != null;
    }

    public int getClaimCount() { return claims.size(); }

    public static final BuilderCodec<ClaimsConfig> CODEC = BuilderCodec.builder(ClaimsConfig.class, ClaimsConfig::new)
            .append(new KeyedCodec<>("SchemaVersion", Codec.INTEGER),
                    (c, v) -> c.schemaVersion = v != null ? v : 1,
                    c -> c.schemaVersion).add()
            .append(new KeyedCodec<>("Claims",
                    new ObjectMapCodec<>(
                            ClaimDefinition.CODEC,
                            HashMap::new,
                            key -> key,
                            str -> str
                    )),
                    (c, v) -> c.claims = v != null ? new HashMap<>(v) : new HashMap<>(),
                    c -> c.claims).add()
            .build();
}
