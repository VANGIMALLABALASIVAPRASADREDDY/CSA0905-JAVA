package com.smartcharge.service;

import com.smartcharge.dao.ChargingSessionDao;
import com.smartcharge.dao.PaymentDao;
import com.smartcharge.dto.PaymentRequest;
import com.smartcharge.model.ChargingSession;
import com.smartcharge.model.Payment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentDao paymentDao;
    private final ChargingSessionDao sessionDao;

    public PaymentService(PaymentDao paymentDao, ChargingSessionDao sessionDao) {
        this.paymentDao = paymentDao;
        this.sessionDao = sessionDao;
    }

    public Payment processPayment(PaymentRequest req) {
        if (req.getSessionId() <= 0) {
            throw new IllegalArgumentException("Valid session ID is required");
        }
        if (req.getAmount() < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        if (req.getPaymentMethod() == null || req.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required (UPI, CARD, CAMPUS_WALLET, CASH)");
        }

        ChargingSession session = sessionDao.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found for ID: " + req.getSessionId()));

        double amount = req.getAmount() > 0 ? req.getAmount() : session.getTotalCost();

        Payment p = new Payment();
        p.setSessionId(req.getSessionId());
        p.setAmount(amount);
        p.setPaymentMethod(req.getPaymentMethod().toUpperCase());
        p.setPaymentStatus("PAID");
        p.setPaymentTime(LocalDateTime.now());

        Payment saved = paymentDao.insert(p);
        return paymentDao.findBySessionId(req.getSessionId()).orElse(saved);
    }

    public List<Payment> getAllPayments() {
        return paymentDao.findAll();
    }

    public List<Payment> getPaymentsByUserId(int userId) {
        return paymentDao.findByUserId(userId);
    }
}
