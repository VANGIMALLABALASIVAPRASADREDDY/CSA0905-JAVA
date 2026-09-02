package com.smartcharge.model;

public class ChargingStation {
    private int stationId;
    private String stationName;
    private String campusLocation;
    private double maximumLoadKw;
    private String status; // 'ACTIVE', 'MAINTENANCE', 'INACTIVE'

    public ChargingStation() {}

    public ChargingStation(int stationId, String stationName, String campusLocation, double maximumLoadKw, String status) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.campusLocation = campusLocation;
        this.maximumLoadKw = maximumLoadKw;
        this.status = status;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
