package com.smartcharge.service;

import com.smartcharge.dao.*;
import com.smartcharge.dto.CheckInRequest;
import com.smartcharge.dto.CheckOutRequest;
import com.smartcharge.exception.ChargerUnavailableException;
import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.ChargingPoint;
import com.smartcharge.model.ChargingSession;
import com.smartcharge.model.Reservation;
import com.smartcharge.model.Vehicle;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChargingSessionService {

    private final ChargingSessionDao sessionDao;
    private final ChargingPointDao pointDao;
    private final ReservationDao reservationDao;
    private final VehicleDao vehicleDao;
    private final TariffDao tariffDao;
    private final EnergyUsageDao energyUsageDao;
    private final LoadManagementService loadManagementService;
    private final QueueService queueService;

    public ChargingSessionService(ChargingSessionDao sessionDao,
                                  ChargingPointDao pointDao,
                                  ReservationDao reservationDao,
                                  VehicleDao vehicleDao,
                                  TariffDao tariffDao,
                                  EnergyUsageDao energyUsageDao,
                                  LoadManagementService loadManagementService,
                                  QueueService queueService) {
        this.sessionDao = sessionDao;
        this.pointDao = pointDao;
        this.reservationDao = reservationDao;
        this.vehicleDao = vehicleDao;
        this.tariffDao = tariffDao;
        this.energyUsageDao = energyUsageDao;
        this.loadManagementService = loadManagementService;
        this.queueService = queueService;
    }

    /**
     * Start a charging session (Check-In)
     */
    public ChargingSession checkIn(CheckInRequest req) {
        ChargingPoint point = pointDao.findById(req.getPointId())
                .orElseThrow(() -> new IllegalArgumentException("Charging point not found for ID: " + req.getPointId()));

        if ("MAINTENANCE".equalsIgnoreCase(point.getStatus())) {
            throw new ChargerUnavailableException("Charging point " + point.getPointName() + " is under maintenance");
        }

        // Validate campus load ceiling
        loadManagementService.validateLoadAllocation(point.getChargerPowerKw());

        Vehicle vehicle = vehicleDao.findById(req.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found for ID: " + req.getVehicleId()));

        ChargingSession cs = new ChargingSession();
        cs.setReservationId(req.getReservationId());
        cs.setVehicleId(req.getVehicleId());
        cs.setPointId(req.getPointId());
        cs.setCheckInTime(LocalDateTime.now());
        cs.setStartingBatteryPercent(req.getStartingBatteryPercent());
        cs.setTargetBatteryPercent(req.getTargetBatteryPercent());
        cs.setEnergyConsumedKwh(0.0);
        cs.setDurationMinutes(0);
        cs.setTotalCost(0.0);
        cs.setStatus("ACTIVE");

        ChargingSession saved = sessionDao.insert(cs);

        // Update charging point status to OCCUPIED
        pointDao.updateStatus(point.getPointId(), "OCCUPIED");

        // Update reservation status to ACTIVE if linked
        if (req.getReservationId() != null && req.getReservationId() > 0) {
            reservationDao.updateStatus(req.getReservationId(), "ACTIVE");
        }

        return sessionDao.findById(saved.getSessionId()).orElse(saved);
    }

    /**
     * Complete a charging session (Check-Out)
     */
    public ChargingSession checkOut(CheckOutRequest req) {
        ChargingSession session = sessionDao.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Charging session not found for ID: " + req.getSessionId()));

        if (!"ACTIVE".equalsIgnoreCase(session.getStatus())) {
            throw new IllegalArgumentException("Session ID " + req.getSessionId() + " is already completed/terminated");
        }

        Vehicle vehicle = vehicleDao.findById(session.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found for session vehicle ID: " + session.getVehicleId()));

        ChargingPoint point = pointDao.findById(session.getPointId())
                .orElseThrow(() -> new IllegalArgumentException("Point not found for session point ID: " + session.getPointId()));

        LocalDateTime checkOutTime = LocalDateTime.now();
        int durationMinutes = (int) Math.max(1, Duration.between(session.getCheckInTime(), checkOutTime).toMinutes());

        double finalBattery = req.getFinalBatteryPercent() > 0 ? req.getFinalBatteryPercent() : session.getTargetBatteryPercent();
        if (finalBattery < session.getStartingBatteryPercent()) {
            finalBattery = session.getTargetBatteryPercent();
        }

        // Energy calculation: Energy = Battery Capacity * (Final% - Starting%) / 100
        double batteryCap = vehicle.getBatteryCapacityKwh();
        double energyConsumedKwh = batteryCap * ((finalBattery - session.getStartingBatteryPercent()) / 100.0);
        if (energyConsumedKwh <= 0.0) {
            energyConsumedKwh = Math.round((point.getChargerPowerKw() * (durationMinutes / 60.0)) * 100.0) / 100.0;
        }
        energyConsumedKwh = Math.round(energyConsumedKwh * 100.0) / 100.0;

        // Cost calculation based on MySQL tariffs table
        double tariffRate = tariffDao.getRateForPower(point.getChargerPowerKw());
        double totalCost = Math.round(energyConsumedKwh * tariffRate * 100.0) / 100.0;

        // Update session in DB
        sessionDao.completeSession(session.getSessionId(), checkOutTime, finalBattery, energyConsumedKwh, durationMinutes, totalCost);

        // Record in energy_usage table
        energyUsageDao.insert(session.getSessionId(), energyConsumedKwh);

        // If linked to reservation, update reservation status to COMPLETED
        if (session.getReservationId() != null) {
            reservationDao.updateStatus(session.getReservationId(), "COMPLETED");
        }

        // Update charging point status to AVAILABLE
        pointDao.updateStatus(point.getPointId(), "AVAILABLE");

        // Trigger automatic queue promotion
        queueService.checkAndPromoteQueuedVehicles(point.getPointId());

        return sessionDao.findById(session.getSessionId()).orElse(session);
    }

    public List<ChargingSession> getActiveSessions() {
        return sessionDao.findActiveSessions();
    }

    public List<ChargingSession> getAllSessions() {
        return sessionDao.findAll();
    }

    public List<ChargingSession> getSessionsByUserId(int userId) {
        return sessionDao.findByUserId(userId);
    }

    public ChargingSession getSessionById(int sessionId) {
        return sessionDao.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found for ID: " + sessionId));
    }
}
