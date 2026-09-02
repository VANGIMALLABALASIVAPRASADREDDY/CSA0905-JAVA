package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargingSessionDto {
    private int sessionId;
    private Integer reservationId;
    private int vehicleId;
    private int pointId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private double startingBatteryPercent;
    private double targetBatteryPercent;
    private Double finalBatteryPercent;
    private double energyConsumedKwh;
    private int durationMinutes;
    private double totalCost;
    private String status;
    private String registrationNumber;
    private String vehicleModel;
    private String pointName;
    private String stationName;
    private String campusLocation;
    private double chargerPowerKw;
    private String userName;

    public ChargingSessionDto() {}

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public double getStartingBatteryPercent() {
        return startingBatteryPercent;
    }

    public void setStartingBatteryPercent(double startingBatteryPercent) {
        this.startingBatteryPercent = startingBatteryPercent;
    }

    public double getTargetBatteryPercent() {
        return targetBatteryPercent;
    }

    public void setTargetBatteryPercent(double targetBatteryPercent) {
        this.targetBatteryPercent = targetBatteryPercent;
    }

    public Double getFinalBatteryPercent() {
        return finalBatteryPercent;
    }

    public void setFinalBatteryPercent(Double finalBatteryPercent) {
        this.finalBatteryPercent = finalBatteryPercent;
    }

    public double getEnergyConsumedKwh() {
        return energyConsumedKwh;
    }

    public void setEnergyConsumedKwh(double energyConsumedKwh) {
        this.energyConsumedKwh = energyConsumedKwh;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
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

    public double getChargerPowerKw() {
        return chargerPowerKw;
    }

    public void setChargerPowerKw(double chargerPowerKw) {
        this.chargerPowerKw = chargerPowerKw;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
