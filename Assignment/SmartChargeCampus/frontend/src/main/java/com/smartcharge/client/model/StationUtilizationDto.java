package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StationUtilizationDto {
    private int stationId;
    private String stationName;
    private String campusLocation;
    private double maximumLoadKw;
    private int totalPoints;
    private int activePoints;
    private int totalSessions;
    private double totalEnergyKwh;
    private double totalRevenue;
    private double utilizationPercent;

    public StationUtilizationDto() {}

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCampusLocation() {
        return campusLocation;
    }

    public void setCampusLocation(String campusLocation) {
        this.campusLocation = campusLocation;
    }

    public double getMaximumLoadKw() {
        return maximumLoadKw;
    }

    public void setMaximumLoadKw(double maximumLoadKw) {
        this.maximumLoadKw = maximumLoadKw;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getActivePoints() {
        return activePoints;
    }

    public void setActivePoints(int activePoints) {
        this.activePoints = activePoints;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getTotalEnergyKwh() {
        return totalEnergyKwh;
    }

    public void setTotalEnergyKwh(double totalEnergyKwh) {
        this.totalEnergyKwh = totalEnergyKwh;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getUtilizationPercent() {
        return utilizationPercent;
    }

    public void setUtilizationPercent(double utilizationPercent) {
        this.utilizationPercent = utilizationPercent;
    }
}
