package com.smartcharge.dto;

import java.time.LocalDateTime;

public class QueueRequest {
    private int userId;
    private int vehicleId;
    private String preferredLocation;
    private double currentBatteryPercent;
    private double targetBatteryPercent;
    private LocalDateTime requestedTime;
    private LocalDateTime departureTime;

    public QueueRequest() {}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public double getCurrentBatteryPercent() {
        return currentBatteryPercent;
    }

    public void setCurrentBatteryPercent(double currentBatteryPercent) {
        this.currentBatteryPercent = currentBatteryPercent;
    }

    public double getTargetBatteryPercent() {
        return targetBatteryPercent;
    }

    public void setTargetBatteryPercent(double targetBatteryPercent) {
        this.targetBatteryPercent = targetBatteryPercent;
    }

    public LocalDateTime getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(LocalDateTime requestedTime) {
        this.requestedTime = requestedTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
}
