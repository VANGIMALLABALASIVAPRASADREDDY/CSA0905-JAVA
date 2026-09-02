package com.smartcharge.dto;

public class PaymentRequest {
    private int sessionId;
    private double amount;
    private String paymentMethod; // UPI, CARD, CAMPUS_WALLET, CASH

    public PaymentRequest() {}

    public PaymentRequest(int sessionId, double amount, String paymentMethod) {
        this.sessionId = sessionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
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
}
