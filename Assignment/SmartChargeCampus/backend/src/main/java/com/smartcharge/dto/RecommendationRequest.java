package com.smartcharge.dto;

import java.time.LocalDateTime;

public class RecommendationRequest {
    private int vehicleId;
    private double currentBatteryPercent;
    private double targetBatteryPercent;
    private String preferredLocation;
    private LocalDateTime requestedStartTime;
    private LocalDateTime expectedDepartureTime;

    public RecommendationRequest() {}

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
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

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public LocalDateTime getRequestedStartTime() {
        return requestedStartTime;
    }

    public void setRequestedStartTime(LocalDateTime requestedStartTime) {
        this.requestedStartTime = requestedStartTime;
    }

    public LocalDateTime getExpectedDepartureTime() {
        return departureTime();
    }

    public LocalDateTime departureTime() {
        return expectedDepartureTime;
    }

    public void setExpectedDepartureTime(LocalDateTime expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }
}
