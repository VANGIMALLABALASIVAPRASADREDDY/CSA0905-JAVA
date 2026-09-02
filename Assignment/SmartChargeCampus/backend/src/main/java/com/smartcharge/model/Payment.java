package com.smartcharge.model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int sessionId;
    private double amount;
    private String paymentMethod; // 'UPI', 'CARD', 'CAMPUS_WALLET', 'CASH'
    private String paymentStatus; // 'PAID', 'PENDING', 'FAILED'
    private LocalDateTime paymentTime;

    // Joined fields
    private String registrationNumber;
    private String userName;
    private String pointName;
    private double energyConsumedKwh;

    public Payment() {}

    public Payment(int paymentId, int sessionId, double amount, String paymentMethod, String paymentStatus, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.sessionId = sessionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentTime = paymentTime;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public double getEnergyConsumedKwh() {
        return energyConsumedKwh;
    }

    public void setEnergyConsumedKwh(double energyConsumedKwh) {
        this.energyConsumedKwh = energyConsumedKwh;
    }
}
