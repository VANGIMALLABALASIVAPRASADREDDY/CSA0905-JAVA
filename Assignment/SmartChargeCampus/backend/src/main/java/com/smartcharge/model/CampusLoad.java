package com.smartcharge.model;

import java.time.LocalDateTime;

public class CampusLoad {
    private int loadId;
    private LocalDateTime recordedTime;
    private double currentEvLoadKw;
    private double maximumEvLoadKw;

    public CampusLoad() {}

    public CampusLoad(int loadId, LocalDateTime recordedTime, double currentEvLoadKw, double maximumEvLoadKw) {
        this.loadId = loadId;
        this.recordedTime = recordedTime;
        this.currentEvLoadKw = currentEvLoadKw;
        this.maximumEvLoadKw = maximumEvLoadKw;
    }

    public int getLoadId() {
        return loadId;
    }

    public void setLoadId(int loadId) {
        this.loadId = loadId;
    }

    public LocalDateTime getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(LocalDateTime recordedTime) {
        this.recordedTime = recordedTime;
    }

    public double getCurrentEvLoadKw() {
        return currentEvLoadKw;
    }

    public void setCurrentEvLoadKw(double currentEvLoadKw) {
        this.currentEvLoadKw = currentEvLoadKw;
    }

    public double getMaximumEvLoadKw() {
        return maximumEvLoadKw;
    }

    public void setMaximumEvLoadKw(double maximumEvLoadKw) {
        this.maximumEvLoadKw = maximumEvLoadKw;
    }
}
