package com.thirdspare.data.economy;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class EconomyConfig {
    private String majorCurrencyName;
    private String minorCurrencyName;
    private long startingBalance;
    private String currencySymbol;
    private boolean useCurrencySymbol;
    private int decimalPlaces;
    private boolean allowNegativeBalances;
    private long maximumBalance;
    private long minimumPaymentAmount;

    public EconomyConfig() {
        this.majorCurrencyName = "Dollars";
        this.minorCurrencyName = "Cents";
        this.startingBalance = 0L;
        this.currencySymbol = "$";
        this.useCurrencySymbol = true;
        this.decimalPlaces = 2;
        this.allowNegativeBalances = false;
        this.maximumBalance = Long.MAX_VALUE;
        this.minimumPaymentAmount = 1L;
    }

    public String getMajorCurrencyName() {
        return majorCurrencyName;
    }

    public void setMajorCurrencyName(String majorCurrencyName) {
        this.majorCurrencyName = majorCurrencyName != null && !majorCurrencyName.isBlank() ? majorCurrencyName : "Dollars";
    }

    public String getMinorCurrencyName() {
        return minorCurrencyName;
    }

    public void setMinorCurrencyName(String minorCurrencyName) {
        this.minorCurrencyName = minorCurrencyName != null && !minorCurrencyName.isBlank() ? minorCurrencyName : "Cents";
    }

    public long getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(long startingBalance) {
        this.startingBalance = startingBalance;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol != null ? currencySymbol : "$";
    }

    public boolean isUseCurrencySymbol() {
        return useCurrencySymbol;
    }

    public void setUseCurrencySymbol(boolean useCurrencySymbol) {
        this.useCurrencySymbol = useCurrencySymbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = Math.max(0, Math.min(8, decimalPlaces));
    }

    public boolean isAllowNegativeBalances() {
        return allowNegativeBalances;
    }

    public void setAllowNegativeBalances(boolean allowNegativeBalances) {
        this.allowNegativeBalances = allowNegativeBalances;
    }

    public long getMaximumBalance() {
        return maximumBalance;
    }

    public void setMaximumBalance(long maximumBalance) {
        this.maximumBalance = maximumBalance > 0L ? maximumBalance : Long.MAX_VALUE;
    }

    public long getMinimumPaymentAmount() {
        return minimumPaymentAmount;
    }

    public void setMinimumPaymentAmount(long minimumPaymentAmount) {
        this.minimumPaymentAmount = Math.max(1L, minimumPaymentAmount);
    }

    public static final BuilderCodec<EconomyConfig> CODEC = BuilderCodec.builder(EconomyConfig.class, EconomyConfig::new)
            .append(new KeyedCodec<>("MajorCurrencyName", Codec.STRING),
                    EconomyConfig::setMajorCurrencyName,
                    EconomyConfig::getMajorCurrencyName).add()
            .append(new KeyedCodec<>("MinorCurrencyName", Codec.STRING),
                    EconomyConfig::setMinorCurrencyName,
                    EconomyConfig::getMinorCurrencyName).add()
            .append(new KeyedCodec<>("StartingBalance", Codec.LONG),
                    EconomyConfig::setStartingBalance,
                    EconomyConfig::getStartingBalance).add()
            .append(new KeyedCodec<>("CurrencySymbol", Codec.STRING),
                    EconomyConfig::setCurrencySymbol,
                    EconomyConfig::getCurrencySymbol).add()
            .append(new KeyedCodec<>("UseCurrencySymbol", Codec.BOOLEAN),
                    EconomyConfig::setUseCurrencySymbol,
                    EconomyConfig::isUseCurrencySymbol).add()
            .append(new KeyedCodec<>("DecimalPlaces", Codec.INTEGER),
                    EconomyConfig::setDecimalPlaces,
                    EconomyConfig::getDecimalPlaces).add()
            .append(new KeyedCodec<>("AllowNegativeBalances", Codec.BOOLEAN),
                    EconomyConfig::setAllowNegativeBalances,
                    EconomyConfig::isAllowNegativeBalances).add()
            .append(new KeyedCodec<>("MaximumBalance", Codec.LONG),
                    EconomyConfig::setMaximumBalance,
                    EconomyConfig::getMaximumBalance).add()
            .append(new KeyedCodec<>("MinimumPaymentAmount", Codec.LONG),
                    EconomyConfig::setMinimumPaymentAmount,
                    EconomyConfig::getMinimumPaymentAmount).add()
            .build();
}
