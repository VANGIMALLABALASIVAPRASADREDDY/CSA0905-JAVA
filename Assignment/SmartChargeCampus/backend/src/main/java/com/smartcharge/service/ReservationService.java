package com.smartcharge.service;

import com.smartcharge.dao.ChargingPointDao;
import com.smartcharge.dao.ReservationDao;
import com.smartcharge.dao.VehicleDao;
import com.smartcharge.dto.ReservationRequest;
import com.smartcharge.exception.ChargerUnavailableException;
import com.smartcharge.exception.InvalidReservationException;
import com.smartcharge.exception.ReservationConflictException;
import com.smartcharge.model.ChargingPoint;
import com.smartcharge.model.Reservation;
import com.smartcharge.model.Vehicle;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ChargingPointDao chargingPointDao;
    private final VehicleDao vehicleDao;
    private final LoadManagementService loadManagementService;

    public ReservationService(ReservationDao reservationDao,
                              ChargingPointDao chargingPointDao,
                              VehicleDao vehicleDao,
                              LoadManagementService loadManagementService) {
        this.reservationDao = reservationDao;
        this.chargingPointDao = chargingPointDao;
        this.vehicleDao = vehicleDao;
        this.loadManagementService = loadManagementService;
    }

    public Reservation createReservation(ReservationRequest req) {
        validateReservationRequest(req);

        ChargingPoint point = chargingPointDao.findById(req.getPointId())
                .orElseThrow(() -> new InvalidReservationException("Charging point not found for ID: " + req.getPointId()));

        if ("MAINTENANCE".equalsIgnoreCase(point.getStatus())) {
            throw new ChargerUnavailableException("Cannot reserve charging point " + point.getPointName() + " as it is currently under MAINTENANCE");
        }

        Vehicle vehicle = vehicleDao.findById(req.getVehicleId())
                .orElseThrow(() -> new InvalidReservationException("Vehicle not found for ID: " + req.getVehicleId()));

        if (!point.getConnectorType().equalsIgnoreCase(vehicle.getConnectorType())) {
            throw new InvalidReservationException(String.format("Connector mismatch! Vehicle requires %s but point provides %s",
                    vehicle.getConnectorType(), point.getConnectorType()));
        }

        // Interval Overlap Check immediately before INSERT
        boolean hasConflict = reservationDao.hasConflict(req.getPointId(), req.getStartTime(), req.getEndTime(), null);
        if (hasConflict) {
            throw new ReservationConflictException("Time slot conflict! Charging point " + point.getPointName() + " is already reserved during this time interval.");
        }

        Reservation res = new Reservation();
        res.setUserId(req.getUserId());
        res.setVehicleId(req.getVehicleId());
        res.setPointId(req.getPointId());
        res.setStartTime(req.getStartTime());
        res.setEndTime(req.getEndTime());
        res.setStatus("CONFIRMED");

        Reservation saved = reservationDao.insert(res);

        // Update point status to RESERVED if start time is near
        if ("AVAILABLE".equalsIgnoreCase(point.getStatus())) {
            chargingPointDao.updateStatus(point.getPointId(), "RESERVED");
        }

        return reservationDao.findById(saved.getReservationId()).orElse(saved);
    }

    public List<Reservation> getReservationsByUserId(int userId) {
        return reservationDao.findByUserId(userId);
    }

    public List<Reservation> getAllReservations() {
        return reservationDao.findAll();
    }

    public Reservation getReservationById(int reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new InvalidReservationException("Reservation not found for ID: " + reservationId));
    }

    public boolean cancelReservation(int reservationId) {
        Reservation res = getReservationById(reservationId);
        boolean updated = reservationDao.updateStatus(reservationId, "CANCELLED");
        if (updated) {
            chargingPointDao.updateStatus(res.getPointId(), "AVAILABLE");
        }
        return updated;
    }

    private void validateReservationRequest(ReservationRequest req) {
        if (req.getUserId() <= 0) {
            throw new InvalidReservationException("Valid user ID is required");
        }
        if (req.getVehicleId() <= 0) {
            throw new InvalidReservationException("Valid vehicle ID is required");
        }
        if (req.getPointId() <= 0) {
            throw new InvalidReservationException("Valid charging point ID is required");
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new InvalidReservationException("Reservation start and end times are required");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new InvalidReservationException("Reservation end time must be strictly after start time");
        }
    }
}
