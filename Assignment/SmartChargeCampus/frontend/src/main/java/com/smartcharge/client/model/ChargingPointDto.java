package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargingPointDto {
    private int pointId;
    private int stationId;
    private String pointName;
    private double chargerPowerKw;
    private String connectorType;
    private String status; // 'AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE'
    private String stationName;
    private String campusLocation;

    public ChargingPointDto() {}

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public double getChargerPowerKw() {
        return chargerPowerKw;
    }

    public void setChargerPowerKw(double chargerPowerKw) {
        this.chargerPowerKw = chargerPowerKw;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return pointName + " (" + chargerPowerKw + " kW, " + connectorType + ") - " + status;
    }
}
