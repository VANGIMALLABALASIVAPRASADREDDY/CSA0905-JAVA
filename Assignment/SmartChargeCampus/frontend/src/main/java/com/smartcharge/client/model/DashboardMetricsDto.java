package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardMetricsDto {
    private int availableChargers;
    private int occupiedChargers;
    private int reservedChargers;
    private int maintenanceChargers;
    private int totalChargers;
    private int activeSessions;
    private int queueLength;
    private double todayEnergyKwh;
    private double todayRevenueInr;
    private double currentCampusLoadKw;
    private double maxCampusLoadKw;
    private double loadPercentage;

    public DashboardMetricsDto() {}

    public int getAvailableChargers() {
        return availableChargers;
    }

    public void setAvailableChargers(int availableChargers) {
        this.availableChargers = availableChargers;
    }

    public int getOccupiedChargers() {
        return occupiedChargers;
    }

    public void setOccupiedChargers(int occupiedChargers) {
        this.occupiedChargers = occupiedChargers;
    }

    public int getReservedChargers() {
        return reservedChargers;
    }

    public void setReservedChargers(int reservedChargers) {
        this.reservedChargers = reservedChargers;
    }

    public int getMaintenanceChargers() {
        return maintenanceChargers;
    }

    public void setMaintenanceChargers(int maintenanceChargers) {
        this.maintenanceChargers = maintenanceChargers;
    }

    public int getTotalChargers() {
        return totalChargers;
    }

    public void setTotalChargers(int totalChargers) {
        this.totalChargers = totalChargers;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(int activeSessions) {
        this.activeSessions = activeSessions;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }

    public double getTodayEnergyKwh() {
        return todayEnergyKwh;
    }

    public void setTodayEnergyKwh(double todayEnergyKwh) {
        this.todayEnergyKwh = todayEnergyKwh;
    }

    public double getTodayRevenueInr() {
        return todayRevenueInr;
    }

    public void setTodayRevenueInr(double todayRevenueInr) {
        this.todayRevenueInr = todayRevenueInr;
    }

    public double getCurrentCampusLoadKw() {
        return currentCampusLoadKw;
    }

    public void setCurrentCampusLoadKw(double currentCampusLoadKw) {
        this.currentCampusLoadKw = currentCampusLoadKw;
    }

    public double getMaxCampusLoadKw() {
        return maxCampusLoadKw;
    }

    public void setMaxCampusLoadKw(double maxCampusLoadKw) {
        this.maxCampusLoadKw = maxCampusLoadKw;
    }

    public double getLoadPercentage() {
        return loadPercentage;
    }

    public void setLoadPercentage(double loadPercentage) {
        this.loadPercentage = loadPercentage;
    }
}
