package com.thirdspare.modules.economy.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

public class EconomyAccount {
    private String uuid;
    private String lastKnownUsername;
    private long balance;

    public EconomyAccount() {
        this.uuid = "";
        this.lastKnownUsername = "";
        this.balance = 0L;
    }

    public EconomyAccount(UUID uuid, String lastKnownUsername, long balance) {
        this.uuid = uuid != null ? uuid.toString() : "";
        this.lastKnownUsername = lastKnownUsername != null ? lastKnownUsername : "";
        this.balance = balance;
    }

    public UUID getUuid() {
        return uuid == null || uuid.isBlank() ? null : UUID.fromString(uuid);
    }

    public String getUuidString() {
        return uuid;
    }

    public void setUuidString(String uuid) {
        this.uuid = uuid != null ? uuid : "";
    }

    public String getLastKnownUsername() {
        return lastKnownUsername;
    }

    public void setLastKnownUsername(String lastKnownUsername) {
        this.lastKnownUsername = lastKnownUsername != null ? lastKnownUsername : "";
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public static final BuilderCodec<EconomyAccount> CODEC = BuilderCodec.builder(EconomyAccount.class, EconomyAccount::new)
            .append(new KeyedCodec<>("Uuid", Codec.STRING),
                    EconomyAccount::setUuidString,
                    EconomyAccount::getUuidString).add()
            .append(new KeyedCodec<>("LastKnownUsername", Codec.STRING),
                    EconomyAccount::setLastKnownUsername,
                    EconomyAccount::getLastKnownUsername).add()
            .append(new KeyedCodec<>("Balance", Codec.LONG),
                    EconomyAccount::setBalance,
                    EconomyAccount::getBalance).add()
            .build();
}
