package com.thirdspare.data.claims;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClaimsConfig {
    private Map<String, Claim> claims;

    public ClaimsConfig() {
        this.claims = new HashMap<>();
    }

    public Collection<Claim> getAllClaims() {
        return claims.values();
    }

    public Claim getClaim(String id) {
        return claims.get(id);
    }

    public void setClaim(Claim claim) {
        if (claim != null && claim.getId() != null && !claim.getId().isBlank()) {
            claims.put(claim.getId(), claim);
        }
    }

    public boolean removeClaim(String id) {
        return claims.remove(id) != null;
    }

    public int getClaimCount() {
        return claims.size();
    }

    public static final BuilderCodec<ClaimsConfig> CODEC = BuilderCodec.builder(ClaimsConfig.class, ClaimsConfig::new)
            .append(new KeyedCodec<>("Claims",
                    new ObjectMapCodec<>(
                            Claim.CODEC,
                            HashMap::new,
                            key -> key,
                            str -> str
                    )),
                    (config, value) -> config.claims = value != null ? new HashMap<>(value) : new HashMap<>(),
                    config -> config.claims).add()
            .build();
}
