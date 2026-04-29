package com.thirdspare.data.economy;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyAccountsConfig {
    private Map<String, EconomyAccount> accounts;

    public EconomyAccountsConfig() {
        this.accounts = new HashMap<>();
    }

    public EconomyAccountsConfig(Map<String, EconomyAccount> accounts) {
        this.accounts = accounts != null ? new HashMap<>(accounts) : new HashMap<>();
        normalizeKeys();
    }

    public Map<String, EconomyAccount> getAccounts() {
        return accounts;
    }

    public Collection<EconomyAccount> getAllAccounts() {
        return accounts.values();
    }

    public EconomyAccount getAccount(UUID uuid) {
        return uuid == null ? null : accounts.get(uuid.toString());
    }

    public EconomyAccount ensureAccount(UUID uuid, String username, long startingBalance) {
        EconomyAccount account = getAccount(uuid);
        if (account == null) {
            account = new EconomyAccount(uuid, username, startingBalance);
            accounts.put(uuid.toString(), account);
        } else if (username != null && !username.isBlank()) {
            account.setLastKnownUsername(username);
        }
        return account;
    }

    public EconomyAccount findByUsername(String username) {
        String normalized = normalizeUsername(username);
        if (normalized.isBlank()) {
            return null;
        }
        for (EconomyAccount account : accounts.values()) {
            if (normalizeUsername(account.getLastKnownUsername()).equals(normalized)) {
                return account;
            }
        }
        return null;
    }

    public EconomyAccount findByNameOrUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            EconomyAccount account = getAccount(UUID.fromString(value.trim()));
            if (account != null) {
                return account;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a UUID; fall back to last known username.
        }
        return findByUsername(value);
    }

    public void normalizeKeys() {
        Map<String, EconomyAccount> normalized = new HashMap<>();
        if (accounts != null) {
            for (EconomyAccount account : accounts.values()) {
                UUID uuid = account != null ? account.getUuid() : null;
                if (uuid != null) {
                    normalized.put(uuid.toString(), account);
                }
            }
        }
        accounts = normalized;
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    public static final BuilderCodec<EconomyAccountsConfig> CODEC = BuilderCodec.builder(EconomyAccountsConfig.class, EconomyAccountsConfig::new)
            .append(new KeyedCodec<>("Accounts",
                    new ObjectMapCodec<>(
                            EconomyAccount.CODEC,
                            HashMap::new,
                            key -> key,
                            str -> str
                    )),
                    (config, value) -> {
                        config.accounts = value != null ? new HashMap<>(value) : new HashMap<>();
                        config.normalizeKeys();
                    },
                    config -> config.accounts).add()
            .build();
}
