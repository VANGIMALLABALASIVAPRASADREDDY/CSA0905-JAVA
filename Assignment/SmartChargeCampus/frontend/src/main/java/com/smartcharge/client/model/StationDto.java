package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StationDto {
    private int stationId;
    private String stationName;
    private String campusLocation;
    private double maximumLoadKw;
    private String status;

    public StationDto() {}

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

    @Override
    public String toString() {
        return stationName + " (" + campusLocation + ")";
    }
}
