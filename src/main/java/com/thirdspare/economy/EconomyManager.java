package com.thirdspare.economy;

import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.data.economy.EconomyAccountsConfig;
import com.thirdspare.data.economy.EconomyConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

public class EconomyManager {
    private final Config<EconomyConfig> economyConfig;
    private final Config<EconomyAccountsConfig> accountsConfig;
    private final EconomyConfig economy;
    private final EconomyAccountsConfig accounts;

    public EconomyManager(Config<EconomyConfig> economyConfig,
                          EconomyConfig economy,
                          Config<EconomyAccountsConfig> accountsConfig,
                          EconomyAccountsConfig accounts) {
        this.economyConfig = economyConfig;
        this.accountsConfig = accountsConfig;
        this.economy = economy;
        this.accounts = accounts;
        ensureDefaults();
    }

    public EconomyConfig getConfig() {
        return economy;
    }

    public EconomyAccountsConfig getAccounts() {
        return accounts;
    }

    public boolean ensureDefaults() {
        boolean changed = false;
        if (economy.getMajorCurrencyName() == null || economy.getMajorCurrencyName().isBlank()) {
            economy.setMajorCurrencyName("Dollars");
            changed = true;
        }
        if (economy.getMinorCurrencyName() == null || economy.getMinorCurrencyName().isBlank()) {
            economy.setMinorCurrencyName("Cents");
            changed = true;
        }
        if (economy.getCurrencySymbol() == null) {
            economy.setCurrencySymbol("$");
            changed = true;
        }
        if (economy.getDecimalPlaces() < 0 || economy.getDecimalPlaces() > 8) {
            economy.setDecimalPlaces(2);
            changed = true;
        }
        if (economy.getMaximumBalance() <= 0L) {
            economy.setMaximumBalance(Long.MAX_VALUE);
            changed = true;
        }
        if (economy.getMinimumPaymentAmount() < 1L) {
            economy.setMinimumPaymentAmount(1L);
            changed = true;
        }
        if (!economy.isAllowNegativeBalances() && economy.getStartingBalance() < 0L) {
            economy.setStartingBalance(0L);
            changed = true;
        }
        if (economy.getStartingBalance() > economy.getMaximumBalance()) {
            economy.setStartingBalance(economy.getMaximumBalance());
            changed = true;
        }
        if (changed) {
            saveConfig();
        }
        return changed;
    }

    public CompletableFuture<Void> saveConfig() {
        return economyConfig.save();
    }

    public CompletableFuture<Void> saveAccounts() {
        return accountsConfig.save();
    }

    public String format(long amount) {
        int decimals = economy.getDecimalPlaces();
        boolean negative = amount < 0L;
        BigDecimal value = BigDecimal.valueOf(Math.abs(amount), decimals).setScale(decimals, RoundingMode.UNNECESSARY);
        String formatted = value.toPlainString();
        if (economy.isUseCurrencySymbol()) {
            formatted = economy.getCurrencySymbol() + formatted;
        }
        return negative ? "-" + formatted : formatted;
    }

    public ParseResult parseAmount(String input) {
        if (input == null || input.isBlank()) {
            return ParseResult.error("Amount is required.");
        }

        String cleaned = input.trim().replace(",", "");
        String symbol = economy.getCurrencySymbol();
        if (symbol != null && !symbol.isBlank() && cleaned.startsWith(symbol)) {
            cleaned = cleaned.substring(symbol.length()).trim();
        }

        BigDecimal value;
        try {
            value = new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            return ParseResult.error("Amount must be a number.");
        }

        int decimals = economy.getDecimalPlaces();
        if (value.scale() > decimals) {
            return ParseResult.error("Amount cannot have more than " + decimals + " decimal place(s).");
        }

        try {
            long minorUnits = value.movePointRight(decimals).longValueExact();
            return new ParseResult(true, minorUnits, null);
        } catch (ArithmeticException ex) {
            return ParseResult.error("Amount is too large.");
        }
    }

    public long getStartingBalance() {
        return economy.getStartingBalance();
    }

    public boolean allowsNegativeBalances() {
        return economy.isAllowNegativeBalances();
    }

    public long getMaximumBalance() {
        return economy.getMaximumBalance();
    }

    public long getMinimumPaymentAmount() {
        return economy.getMinimumPaymentAmount();
    }

    public record ParseResult(boolean success, long amount, String errorMessage) {
        private static ParseResult error(String message) {
            return new ParseResult(false, 0L, message);
        }
    }
}
