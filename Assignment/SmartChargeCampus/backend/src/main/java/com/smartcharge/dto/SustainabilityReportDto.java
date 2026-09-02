package com.smartcharge.dto;

public class SustainabilityReportDto {
    private double totalEnergyDeliveredKwh;
    private int totalSessions;
    private double averageChargerUtilizationPercent;
    private double peakCampusChargingLoadKw;
    private double maxCampusCapacityKw;
    private double averageWaitTimeMinutes;
    // Standard conversion factor: 0.82 kg CO2 saved per kWh for Indian Grid EV vs ICE average
    private double co2SavedKg;
    private String co2ConversionBasis;

    public SustainabilityReportDto() {}

    public double getTotalEnergyDeliveredKwh() {
        return totalEnergyDeliveredKwh;
    }

    public void setTotalEnergyDeliveredKwh(double totalEnergyDeliveredKwh) {
        this.totalEnergyDeliveredKwh = totalEnergyDeliveredKwh;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getAverageChargerUtilizationPercent() {
        return averageChargerUtilizationPercent;
    }

    public void setAverageChargerUtilizationPercent(double averageChargerUtilizationPercent) {
        this.averageChargerUtilizationPercent = averageChargerUtilizationPercent;
    }

    public double getPeakCampusChargingLoadKw() {
        return peakCampusChargingLoadKw;
    }

    public void setPeakCampusChargingLoadKw(double peakCampusChargingLoadKw) {
        this.peakCampusChargingLoadKw = peakCampusChargingLoadKw;
    }

    public double getMaxCampusCapacityKw() {
        return maxCampusCapacityKw;
    }

    public void setMaxCampusCapacityKw(double maxCampusCapacityKw) {
        this.maxCampusCapacityKw = maxCampusCapacityKw;
    }

    public double getAverageWaitTimeMinutes() {
        return averageWaitTimeMinutes;
    }

    public void setAverageWaitTimeMinutes(double averageWaitTimeMinutes) {
        this.averageWaitTimeMinutes = averageWaitTimeMinutes;
    }

    public double getCo2SavedKg() {
        return co2SavedKg;
    }

    public void setCo2SavedKg(double co2SavedKg) {
        this.co2SavedKg = co2SavedKg;
    }

    public String getCo2ConversionBasis() {
        return co2ConversionBasis;
    }

    public void setCo2ConversionBasis(String co2ConversionBasis) {
        this.co2ConversionBasis = co2ConversionBasis;
    }
}
