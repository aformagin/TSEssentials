package com.thirdspare.economy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.data.economy.EconomyAccount;
import com.thirdspare.data.economy.EconomyAccountsConfig;
import com.thirdspare.utils.PlayerLookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyService {
    private final EconomyManager economyManager;
    private final ComponentType<EntityStore, PlayerEconomyComponent> economyComponentType;
    private final ConcurrentHashMap<UUID, PlayerEconomyComponent> runtimeEconomy = new ConcurrentHashMap<>();

    public EconomyService(EconomyManager economyManager,
                          ComponentType<EntityStore, PlayerEconomyComponent> economyComponentType) {
        this.economyManager = economyManager;
        this.economyComponentType = economyComponentType;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public void loadEconomy(PlayerRef player) {
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }

        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                EconomyAccount account;
                synchronized (economyManager.getAccounts()) {
                    account = economyManager.getAccounts().ensureAccount(
                            player.getUuid(),
                            player.getUsername(),
                            economyManager.getStartingBalance()
                    );
                    economyManager.saveAccounts();
                }

                Ref<EntityStore> ref = player.getReference();
                Store<EntityStore> store = ref.getStore();
                PlayerEconomyComponent storedEconomy = store.ensureAndGetComponent(ref, economyComponentType);
                synchronized (account) {
                    storedEconomy.setBalance(account.getBalance());
                    runtimeEconomy.put(player.getUuid(), new PlayerEconomyComponent(storedEconomy));
                }
            } catch (IllegalStateException ignored) {
                runtimeEconomy.computeIfAbsent(player.getUuid(), ignoredUuid -> {
                    PlayerEconomyComponent economy = new PlayerEconomyComponent();
                    economy.setBalance(economyManager.getStartingBalance());
                    return economy;
                });
            }
        });
    }

    public AccountView getAccountView(PlayerRef player) {
        EconomyAccount account = ensureOnlineAccount(player);
        PlayerEconomyComponent runtime = runtimeEconomy.computeIfAbsent(player.getUuid(), ignored -> {
            PlayerEconomyComponent component = new PlayerEconomyComponent();
            synchronized (account) {
                component.setBalance(account.getBalance());
            }
            return component;
        });
        synchronized (runtime) {
            return new AccountView(player.getUuid(), player.getUsername(), runtime.getBalance(), true, player);
        }
    }

    public AccountView getAccountView(String target) {
        PlayerRef online = PlayerLookup.findPlayerByName(target).orElse(null);
        if (online != null) {
            return getAccountView(online);
        }

        EconomyAccount account;
        synchronized (economyManager.getAccounts()) {
            account = economyManager.getAccounts().findByNameOrUuid(target);
        }
        if (account == null || account.getUuid() == null) {
            return null;
        }
        synchronized (account) {
            return new AccountView(account.getUuid(), displayName(account), account.getBalance(), false, null);
        }
    }

    public List<AccountView> getAccountViews(int limit) {
        List<AccountView> views = new ArrayList<>();
        synchronized (economyManager.getAccounts()) {
            for (EconomyAccount account : economyManager.getAccounts().getAllAccounts()) {
                UUID uuid = account.getUuid();
                if (uuid == null) {
                    continue;
                }
                PlayerRef online = Universe.get().getPlayer(uuid);
                synchronized (account) {
                    views.add(new AccountView(
                            uuid,
                            displayName(account),
                            account.getBalance(),
                            online != null && online.isValid(),
                            online
                    ));
                }
            }
        }
        views.sort(Comparator.comparing(AccountView::displayName, String.CASE_INSENSITIVE_ORDER));
        return limit > 0 && views.size() > limit ? new ArrayList<>(views.subList(0, limit)) : views;
    }

    public MutationResult setBalance(String target, long amount) {
        AccountTarget accountTarget = resolveTarget(target);
        if (accountTarget == null) {
            return MutationResult.error("Unknown player: " + target);
        }
        if (!economyManager.allowsNegativeBalances() && amount < 0L) {
            return MutationResult.error("Balance cannot be negative.");
        }
        if (amount > economyManager.getMaximumBalance()) {
            return MutationResult.error("Balance cannot exceed " + economyManager.format(economyManager.getMaximumBalance()) + ".");
        }

        synchronized (accountTarget.account()) {
            accountTarget.account().setBalance(amount);
        }
        persistMutation(accountTarget);
        return new MutationResult(true, null, view(accountTarget));
    }

    public MutationResult deposit(String target, long amount) {
        String amountError = validatePositiveAmount(amount, true);
        if (amountError != null) {
            return MutationResult.error(amountError);
        }

        AccountTarget accountTarget = resolveTarget(target);
        if (accountTarget == null) {
            return MutationResult.error("Unknown player: " + target);
        }

        synchronized (accountTarget.account()) {
            String depositError = validateDeposit(accountTarget.account().getBalance(), amount);
            if (depositError != null) {
                return MutationResult.error(depositError);
            }
            accountTarget.account().setBalance(accountTarget.account().getBalance() + amount);
        }
        persistMutation(accountTarget);
        return new MutationResult(true, null, view(accountTarget));
    }

    public MutationResult withdraw(String target, long amount) {
        String amountError = validatePositiveAmount(amount, true);
        if (amountError != null) {
            return MutationResult.error(amountError);
        }

        AccountTarget accountTarget = resolveTarget(target);
        if (accountTarget == null) {
            return MutationResult.error("Unknown player: " + target);
        }

        synchronized (accountTarget.account()) {
            String withdrawError = validateWithdraw(accountTarget.account().getBalance(), amount);
            if (withdrawError != null) {
                return MutationResult.error(withdrawError);
            }
            accountTarget.account().setBalance(accountTarget.account().getBalance() - amount);
        }
        persistMutation(accountTarget);
        return new MutationResult(true, null, view(accountTarget));
    }

    public TransferResult transfer(PlayerRef sender, String recipientName, long amount) {
        String amountError = validatePositiveAmount(amount, true);
        if (amountError != null) {
            return TransferResult.error(amountError);
        }

        AccountTarget senderTarget = resolveTarget(sender.getUsername());
        if (senderTarget == null) {
            return TransferResult.error("Unable to find your economy account.");
        }

        AccountTarget recipientTarget = resolveTarget(recipientName);
        if (recipientTarget == null) {
            return TransferResult.error("Unknown player: " + recipientName);
        }

        if (senderTarget.uuid().equals(recipientTarget.uuid())) {
            return TransferResult.error("You cannot pay yourself.");
        }

        EconomyAccount first = senderTarget.uuid().compareTo(recipientTarget.uuid()) <= 0
                ? senderTarget.account()
                : recipientTarget.account();
        EconomyAccount second = first == senderTarget.account() ? recipientTarget.account() : senderTarget.account();

        synchronized (first) {
            synchronized (second) {
                String withdrawError = validateWithdraw(senderTarget.account().getBalance(), amount);
                if (withdrawError != null) {
                    return TransferResult.error(withdrawError);
                }
                String depositError = validateDeposit(recipientTarget.account().getBalance(), amount);
                if (depositError != null) {
                    return TransferResult.error(depositError);
                }
                senderTarget.account().setBalance(senderTarget.account().getBalance() - amount);
                recipientTarget.account().setBalance(recipientTarget.account().getBalance() + amount);
            }
        }

        persistMutation(senderTarget);
        persistMutation(recipientTarget);
        economyManager.saveAccounts();
        return new TransferResult(true, null, view(senderTarget), view(recipientTarget));
    }

    public boolean hasFunds(PlayerRef player, long amount) {
        AccountView view = getAccountView(player);
        return view.balance() >= amount;
    }

    public void saveEconomy(PlayerRef player, PlayerEconomyComponent economy) {
        PlayerEconomyComponent snapshot;
        synchronized (economy) {
            snapshot = new PlayerEconomyComponent(economy);
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
                Ref<EntityStore> ref = player.getReference();
                Store<EntityStore> store = ref.getStore();
                PlayerEconomyComponent storedEconomy = store.ensureAndGetComponent(ref, economyComponentType);
                storedEconomy.setBalance(snapshot.getBalance());
            } catch (IllegalStateException ignored) {
                // The ledger is authoritative; a later login will resync ECS.
            }
        });
    }

    private EconomyAccount ensureOnlineAccount(PlayerRef player) {
        synchronized (economyManager.getAccounts()) {
            EconomyAccount account = economyManager.getAccounts().ensureAccount(
                    player.getUuid(),
                    player.getUsername(),
                    economyManager.getStartingBalance()
            );
            economyManager.saveAccounts();
            return account;
        }
    }

    private AccountTarget resolveTarget(String target) {
        PlayerRef online = PlayerLookup.findPlayerByName(target).orElse(null);
        if (online != null) {
            EconomyAccount account = ensureOnlineAccount(online);
            return new AccountTarget(online.getUuid(), online.getUsername(), account, online);
        }

        EconomyAccount account;
        synchronized (economyManager.getAccounts()) {
            account = economyManager.getAccounts().findByNameOrUuid(target);
        }
        if (account == null || account.getUuid() == null) {
            return null;
        }
        return new AccountTarget(account.getUuid(), displayName(account), account, null);
    }

    private void persistMutation(AccountTarget target) {
        if (target.onlinePlayer() != null && target.onlinePlayer().isValid()) {
            PlayerEconomyComponent runtime = runtimeEconomy.computeIfAbsent(target.uuid(), ignored -> new PlayerEconomyComponent());
            synchronized (runtime) {
                synchronized (target.account()) {
                    runtime.setBalance(target.account().getBalance());
                }
                saveEconomy(target.onlinePlayer(), runtime);
            }
        }
        economyManager.saveAccounts();
    }

    private AccountView view(AccountTarget target) {
        synchronized (target.account()) {
            return new AccountView(
                    target.uuid(),
                    target.displayName(),
                    target.account().getBalance(),
                    target.onlinePlayer() != null && target.onlinePlayer().isValid(),
                    target.onlinePlayer()
            );
        }
    }

    private String displayName(EconomyAccount account) {
        String name = account.getLastKnownUsername();
        UUID uuid = account.getUuid();
        return name == null || name.isBlank() ? (uuid != null ? uuid.toString() : "Unknown") : name;
    }

    private String validatePositiveAmount(long amount, boolean enforceMinimumPayment) {
        if (amount <= 0L) {
            return "Amount must be greater than zero.";
        }
        if (enforceMinimumPayment && amount < economyManager.getMinimumPaymentAmount()) {
            return "Amount must be at least " + economyManager.format(economyManager.getMinimumPaymentAmount()) + ".";
        }
        return null;
    }

    private String validateDeposit(long balance, long amount) {
        if (amount > 0L && Long.MAX_VALUE - balance < amount) {
            return "Balance would overflow.";
        }
        if (balance + amount > economyManager.getMaximumBalance()) {
            return "Balance cannot exceed " + economyManager.format(economyManager.getMaximumBalance()) + ".";
        }
        return null;
    }

    private String validateWithdraw(long balance, long amount) {
        if (amount > 0L && Long.MIN_VALUE + amount > balance) {
            return "Balance would underflow.";
        }
        if (!economyManager.allowsNegativeBalances() && balance < amount) {
            return "You do not have enough funds.";
        }
        return null;
    }

    private record AccountTarget(UUID uuid, String displayName, EconomyAccount account, PlayerRef onlinePlayer) {
    }

    public record AccountView(UUID uuid, String displayName, long balance, boolean online, PlayerRef onlinePlayer) {
    }

    public record MutationResult(boolean success, String error, AccountView account) {
        private static MutationResult error(String error) {
            return new MutationResult(false, error, null);
        }
    }

    public record TransferResult(boolean success, String error, AccountView sender, AccountView recipient) {
        private static TransferResult error(String error) {
            return new TransferResult(false, error, null, null);
        }
    }
}
