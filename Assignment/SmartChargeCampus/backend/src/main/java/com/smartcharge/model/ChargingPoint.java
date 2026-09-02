package com.smartcharge.model;

public class ChargingPoint {
    private int pointId;
    private int stationId;
    private String pointName;
    private double chargerPowerKw;
    private String connectorType; // 'Type 2', 'CCS2', 'CHAdeMO', 'GB/T'
    private String status; // 'AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE'

    // Joined fields
    private String stationName;
    private String campusLocation;

    public ChargingPoint() {}

    public ChargingPoint(int pointId, int stationId, String pointName, double chargerPowerKw, 
                         String connectorType, String status) {
        this.pointId = pointId;
        this.stationId = stationId;
        this.pointName = pointName;
        this.chargerPowerKw = chargerPowerKw;
        this.connectorType = connectorType;
        this.status = status;
    }

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
}
