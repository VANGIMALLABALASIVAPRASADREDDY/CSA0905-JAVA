package com.smartcharge.model;

import java.time.LocalDateTime;

public class Vehicle {
    private int vehicleId;
    private int userId;
    private String registrationNumber;
    private String manufacturer;
    private String model;
    private double batteryCapacityKwh;
    private String connectorType; // 'Type 2', 'CCS2', 'CHAdeMO', 'GB/T'
    private LocalDateTime createdAt;
    
    // Additional populated fields for display
    private String ownerName;

    public Vehicle() {}

    public Vehicle(int vehicleId, int userId, String registrationNumber, String manufacturer, 
                   String model, double batteryCapacityKwh, String connectorType, LocalDateTime createdAt) {
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.registrationNumber = registrationNumber;
        this.manufacturer = manufacturer;
        this.model = model;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.connectorType = connectorType;
        this.createdAt = createdAt;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getBatteryCapacityKwh() {
        return batteryCapacityKwh;
    }

    public void setBatteryCapacityKwh(double batteryCapacityKwh) {
        this.batteryCapacityKwh = batteryCapacityKwh;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
