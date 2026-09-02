package com.smartcharge.service;

import com.smartcharge.dao.ChargingPointDao;
import com.smartcharge.dao.QueueDao;
import com.smartcharge.dao.ReservationDao;
import com.smartcharge.dao.VehicleDao;
import com.smartcharge.dto.QueueRequest;
import com.smartcharge.exception.InvalidVehicleException;
import com.smartcharge.model.ChargingPoint;
import com.smartcharge.model.QueueEntry;
import com.smartcharge.model.Reservation;
import com.smartcharge.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final QueueDao queueDao;
    private final VehicleDao vehicleDao;
    private final ChargingPointDao chargingPointDao;
    private final ReservationDao reservationDao;
    private final LoadManagementService loadManagementService;

    public QueueService(QueueDao queueDao,
                        VehicleDao vehicleDao,
                        ChargingPointDao chargingPointDao,
                        ReservationDao reservationDao,
                        LoadManagementService loadManagementService) {
        this.queueDao = queueDao;
        this.vehicleDao = vehicleDao;
        this.chargingPointDao = chargingPointDao;
        this.reservationDao = reservationDao;
        this.loadManagementService = loadManagementService;
    }

    public QueueEntry joinQueue(QueueRequest req) {
        validateQueueRequest(req);

        Vehicle vehicle = vehicleDao.findById(req.getVehicleId())
                .orElseThrow(() -> new InvalidVehicleException("Vehicle not found for ID: " + req.getVehicleId()));

        double priorityScore = calculatePriorityScore(
                req.getCurrentBatteryPercent(),
                req.getRequestedTime(),
                req.getDepartureTime(),
                LocalDateTime.now()
        );

        int currentCount = queueDao.getActiveQueueCount();

        QueueEntry qe = new QueueEntry();
        qe.setUserId(req.getUserId());
        qe.setVehicleId(req.getVehicleId());
        qe.setPreferredLocation(req.getPreferredLocation() != null ? req.getPreferredLocation() : "Engineering Block");
        qe.setCurrentBatteryPercent(req.getCurrentBatteryPercent());
        qe.setTargetBatteryPercent(req.getTargetBatteryPercent());
        qe.setRequestedTime(req.getRequestedTime());
        qe.setDepartureTime(req.getDepartureTime());
        qe.setPriorityScore(priorityScore);
        qe.setQueuePosition(currentCount + 1);
        qe.setStatus("WAITING");

        QueueEntry saved = queueDao.insert(qe);
        return queueDao.findById(saved.getQueueId()).orElse(saved);
    }

    public List<QueueEntry> getActiveQueue() {
        return queueDao.findActiveQueue();
    }

    public List<QueueEntry> getAllQueueEntries() {
        return queueDao.findAll();
    }

    /**
     * Priority Score Calculation:
     * Priority = Battery Urgency + Departure Urgency + Waiting Time Points
     */
    public double calculatePriorityScore(double batteryPercent, LocalDateTime requestedTime, 
                                         LocalDateTime departureTime, LocalDateTime createdTime) {
        // 1. Battery Urgency
        double batteryUrgency;
        if (batteryPercent <= 15.0) {
            batteryUrgency = 40.0;
        } else if (batteryPercent <= 30.0) {
            batteryUrgency = 30.0;
        } else if (batteryPercent <= 50.0) {
            batteryUrgency = 20.0;
        } else {
            batteryUrgency = 10.0;
        }

        // 2. Departure Urgency
        double departureUrgency = 10.0;
        if (departureTime != null) {
            long hoursUntilDeparture = Duration.between(LocalDateTime.now(), departureTime).toHours();
            if (hoursUntilDeparture < 1) {
                departureUrgency = 40.0;
            } else if (hoursUntilDeparture <= 2) {
                departureUrgency = 30.0;
            } else if (hoursUntilDeparture <= 4) {
                departureUrgency = 20.0;
            } else {
                departureUrgency = 10.0;
            }
        }

        // 3. Waiting Duration Points (+1 point per 5 minutes in queue)
        long waitingMins = Duration.between(createdTime, LocalDateTime.now()).toMinutes();
        double waitBonus = Math.max(0.0, (waitingMins / 5.0));

        return Math.round((batteryUrgency + departureUrgency + waitBonus) * 10.0) / 10.0;
    }

    /**
     * Automatically promotes the highest priority queued vehicle when a charging point becomes available
     */
    public void checkAndPromoteQueuedVehicles(int availablePointId) {
        ChargingPoint point = chargingPointDao.findById(availablePointId).orElse(null);
        if (point == null || !"AVAILABLE".equalsIgnoreCase(point.getStatus())) {
            return;
        }

        List<QueueEntry> activeQueue = queueDao.findActiveQueue();
        for (QueueEntry entry : activeQueue) {
            if (!entry.getConnectorType().equalsIgnoreCase(point.getConnectorType())) {
                continue; // connector mismatch
            }

            // Check campus load accommodates this point
            if (!loadManagementService.canAccommodateLoad(point.getChargerPowerKw())) {
                continue;
            }

            // Estimate duration and check conflicts
            double energyRequired = entry.getBatteryCapacityKwh() * ((entry.getTargetBatteryPercent() - entry.getCurrentBatteryPercent()) / 100.0);
            int durationMins = (int) Math.ceil((energyRequired / point.getChargerPowerKw()) * 60.0);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime estimatedEnd = now.plusMinutes(durationMins);

            boolean hasConflict = reservationDao.hasConflict(point.getPointId(), now, estimatedEnd, null);
            if (!hasConflict) {
                // Promote this queued entry
                logger.info("Promoting Queue Entry ID: {} for Vehicle: {} on Point: {}",
                        entry.getQueueId(), entry.getRegistrationNumber(), point.getPointName());

                queueDao.updateStatus(entry.getQueueId(), "PROMOTED");
                chargingPointDao.updateStatus(point.getPointId(), "RESERVED");

                // Auto-create reservation for the promoted user
                Reservation res = new Reservation();
                res.setUserId(entry.getUserId());
                res.setVehicleId(entry.getVehicleId());
                res.setPointId(point.getPointId());
                res.setStartTime(now);
                res.setEndTime(estimatedEnd);
                res.setStatus("CONFIRMED");
                reservationDao.insert(res);

                break; // One promotion per newly freed point
            }
        }
    }

    private void validateQueueRequest(QueueRequest req) {
        if (req.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }
        if (req.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Valid vehicle ID is required");
        }
        if (req.getCurrentBatteryPercent() < 0.0 || req.getCurrentBatteryPercent() > 100.0) {
            throw new IllegalArgumentException("Current battery percentage must be between 0% and 100%");
        }
        if (req.getTargetBatteryPercent() <= req.getCurrentBatteryPercent()) {
            throw new IllegalArgumentException("Target battery percentage must be greater than current battery percentage");
        }
    }
}
