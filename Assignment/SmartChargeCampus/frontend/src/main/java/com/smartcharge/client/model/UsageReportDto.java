package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageReportDto {
    private int totalSessions;
    private double averageDurationMinutes;
    private String mostUsedStation;
    private String mostUsedCharger;
    private String peakChargingPeriod;
    private double averageWaitingTimeMinutes;
    private double totalEnergyDeliveredKwh;
    private double totalRevenueInr;

    public UsageReportDto() {}

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getAverageDurationMinutes() {
        return averageDurationMinutes;
    }

    public void setAverageDurationMinutes(double averageDurationMinutes) {
        this.averageDurationMinutes = averageDurationMinutes;
    }

    public String getMostUsedStation() {
        return mostUsedStation;
    }

    public void setMostUsedStation(String mostUsedStation) {
        this.mostUsedStation = mostUsedStation;
    }

    public String getMostUsedCharger() {
        return mostUsedCharger;
    }

    public void setMostUsedCharger(String mostUsedCharger) {
        this.mostUsedCharger = mostUsedCharger;
    }

    public String getPeakChargingPeriod() {
        return peakChargingPeriod;
    }

    public void setPeakChargingPeriod(String peakChargingPeriod) {
        this.peakChargingPeriod = peakChargingPeriod;
    }

    public double getAverageWaitingTimeMinutes() {
        return averageWaitingTimeMinutes;
    }

    public void setAverageWaitingTimeMinutes(double averageWaitingTimeMinutes) {
        this.averageWaitingTimeMinutes = averageWaitingTimeMinutes;
    }

    public double getTotalEnergyDeliveredKwh() {
        return totalEnergyDeliveredKwh;
    }

    public void setTotalEnergyDeliveredKwh(double totalEnergyDeliveredKwh) {
        this.totalEnergyDeliveredKwh = totalEnergyDeliveredKwh;
    }

    public double getTotalRevenueInr() {
        return totalRevenueInr;
    }

    public void setTotalRevenueInr(double totalRevenueInr) {
        this.totalRevenueInr = totalRevenueInr;
    }
}
