package com.smartcharge.model;

public class Tariff {
    private int tariffId;
    private double chargerPowerKw;
    private double ratePerKwh;

    public Tariff() {}

    public Tariff(int tariffId, double chargerPowerKw, double ratePerKwh) {
        this.tariffId = tariffId;
        this.chargerPowerKw = chargerPowerKw;
        this.ratePerKwh = ratePerKwh;
    }

    public int getTariffId() {
        return tariffId;
    }

    public void setTariffId(int tariffId) {
        this.tariffId = tariffId;
    }

    public double getChargerPowerKw() {
        return chargerPowerKw;
    }

    public void setChargerPowerKw(double chargerPowerKw) {
        this.chargerPowerKw = chargerPowerKw;
    }

    public double getRatePerKwh() {
        return ratePerKwh;
    }

    public void setRatePerKwh(double ratePerKwh) {
        this.ratePerKwh = ratePerKwh;
    }
}
