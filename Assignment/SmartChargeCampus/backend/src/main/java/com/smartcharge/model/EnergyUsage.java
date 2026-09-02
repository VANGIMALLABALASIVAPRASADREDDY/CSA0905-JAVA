package com.smartcharge.model;

import java.time.LocalDateTime;

public class EnergyUsage {
    private int usageId;
    private int sessionId;
    private double energyKwh;
    private LocalDateTime recordedAt;

    public EnergyUsage() {}

    public EnergyUsage(int usageId, int sessionId, double energyKwh, LocalDateTime recordedAt) {
        this.usageId = usageId;
        this.sessionId = sessionId;
        this.energyKwh = energyKwh;
        this.recordedAt = recordedAt;
    }

    public int getUsageId() {
        return usageId;
    }

    public void setUsageId(int usageId) {
        this.usageId = usageId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(double energyKwh) {
        this.energyKwh = energyKwh;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
