package com.thirdspare.economy;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerEconomyComponent implements Component<EntityStore> {
    private long balance;

    public PlayerEconomyComponent() {
        this.balance = 0L;
    }

    public PlayerEconomyComponent(PlayerEconomyComponent other) {
        this.balance = other != null ? other.balance : 0L;
    }

    @Override
    public Component<EntityStore> clone() {
        return new PlayerEconomyComponent(this);
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public void withdraw(long amount) {
        this.balance -= amount;
    }

    public boolean hasAtLeast(long amount) {
        return balance >= amount;
    }

    public static final BuilderCodec<PlayerEconomyComponent> CODEC =
            BuilderCodec.builder(PlayerEconomyComponent.class, PlayerEconomyComponent::new)
                    .append(new KeyedCodec<>("Balance", Codec.LONG),
                            PlayerEconomyComponent::setBalance,
                            PlayerEconomyComponent::getBalance).add()
                    .build();
}
