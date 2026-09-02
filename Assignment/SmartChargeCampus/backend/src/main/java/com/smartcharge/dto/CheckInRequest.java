package com.smartcharge.dto;

public class CheckInRequest {
    private Integer reservationId;
    private int vehicleId;
    private int pointId;
    private double startingBatteryPercent;
    private double targetBatteryPercent;

    public CheckInRequest() {}

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
}
