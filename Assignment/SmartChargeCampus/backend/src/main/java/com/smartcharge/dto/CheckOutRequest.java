package com.smartcharge.dto;

public class CheckOutRequest {
    private int sessionId;
    private double finalBatteryPercent;

    public CheckOutRequest() {}

    public CheckOutRequest(int sessionId, double finalBatteryPercent) {
        this.sessionId = sessionId;
        this.finalBatteryPercent = finalBatteryPercent;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public double getFinalBatteryPercent() {
        return finalBatteryPercent;
    }

    public void setFinalBatteryPercent(double finalBatteryPercent) {
        this.finalBatteryPercent = finalBatteryPercent;
    }
}
