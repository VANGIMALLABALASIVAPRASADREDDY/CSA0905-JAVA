package com.smartcharge.service;

import com.smartcharge.dao.ChargingPointDao;
import com.smartcharge.dao.ReservationDao;
import com.smartcharge.dao.TariffDao;
import com.smartcharge.dao.VehicleDao;
import com.smartcharge.dto.CandidateScoreDto;
import com.smartcharge.dto.RecommendationRequest;
import com.smartcharge.dto.RecommendationResponse;
import com.smartcharge.exception.InvalidVehicleException;
import com.smartcharge.model.ChargingPoint;
import com.smartcharge.model.Vehicle;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChargingRecommendationService {

    private final VehicleDao vehicleDao;
    private final ChargingPointDao chargingPointDao;
    private final ReservationDao reservationDao;
    private final TariffDao tariffDao;
    private final LoadManagementService loadManagementService;

    public ChargingRecommendationService(VehicleDao vehicleDao,
                                         ChargingPointDao chargingPointDao,
                                         ReservationDao reservationDao,
                                         TariffDao tariffDao,
                                         LoadManagementService loadManagementService) {
        this.vehicleDao = vehicleDao;
        this.chargingPointDao = chargingPointDao;
        this.reservationDao = reservationDao;
        this.tariffDao = tariffDao;
        this.loadManagementService = loadManagementService;
    }

    public RecommendationResponse recommendBestCharger(RecommendationRequest req) {
        validateRequest(req);

        Vehicle vehicle = vehicleDao.findById(req.getVehicleId())
                .orElseThrow(() -> new InvalidVehicleException("Vehicle not found for ID: " + req.getVehicleId()));

        double currentLoad = loadManagementService.getCurrentActiveLoad();
        double maxLoad = loadManagementService.getMaxCampusCapacity();

        // 1. Calculate Required Energy
        // Formula: Energy Required = Battery Capacity * (Target% - Current%) / 100
        double batteryCap = vehicle.getBatteryCapacityKwh();
        double energyRequiredKwh = batteryCap * ((req.getTargetBatteryPercent() - req.getCurrentBatteryPercent()) / 100.0);
        energyRequiredKwh = Math.round(energyRequiredKwh * 100.0) / 100.0;

        LocalDateTime startTime = req.getRequestedStartTime() != null ? req.getRequestedStartTime() : LocalDateTime.now();
        LocalDateTime departureTime = req.getExpectedDepartureTime();

        // 2. Fetch compatible charging points
        List<ChargingPoint> compatiblePoints = chargingPointDao.findCompatiblePoints(vehicle.getConnectorType());
        List<CandidateScoreDto> candidateScores = new ArrayList<>();

        for (ChargingPoint point : compatiblePoints) {
            CandidateScoreDto candidate = evaluatePoint(point, vehicle, req, energyRequiredKwh, startTime, departureTime, currentLoad, maxLoad);
            candidateScores.add(candidate);
        }

        // 3. Sort candidates by Total Score descending
        candidateScores.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

        // 4. Identify best eligible candidate
        RecommendationResponse response = new RecommendationResponse();
        response.setCurrentCampusLoadKw(currentLoad);
        response.setMaxCampusLoadKw(maxLoad);
        response.setRankedCandidates(candidateScores);

        CandidateScoreDto best = candidateScores.stream().filter(CandidateScoreDto::isEligible).findFirst().orElse(null);

        if (best != null && best.getTotalScore() > 0) {
            response.setMatchFound(true);
            response.setBestCharger(best);
            response.setVirtualQueueRecommended(false);
            response.setMessage("Optimal load-aware charger found: " + best.getPointName() + " (" + best.getStationName() + ")");
        } else {
            response.setMatchFound(false);
            response.setBestCharger(null);
            response.setVirtualQueueRecommended(true);
            response.setMessage("No available charger meets all constraints at this moment. You can join the Virtual Queue.");
        }

        return response;
    }

    private CandidateScoreDto evaluatePoint(ChargingPoint point, Vehicle vehicle, RecommendationRequest req,
                                            double energyRequiredKwh, LocalDateTime startTime, LocalDateTime departureTime,
                                            double currentLoad, double maxLoad) {
        CandidateScoreDto dto = new CandidateScoreDto();
        dto.setPointId(point.getPointId());
        dto.setPointName(point.getPointName());
        dto.setStationId(point.getStationId());
        dto.setStationName(point.getStationName());
        dto.setCampusLocation(point.getCampusLocation());
        dto.setChargerPowerKw(point.getChargerPowerKw());
        dto.setConnectorType(point.getConnectorType());
        dto.setStatus(point.getStatus());
        dto.setRequiredEnergyKwh(energyRequiredKwh);

        // Calculate Charging Time: Estimated Hours = Energy Required / Charger Power
        double estimatedHours = energyRequiredKwh / point.getChargerPowerKw();
        int estimatedMinutes = (int) Math.ceil(estimatedHours * 60.0);
        dto.setEstimatedDurationHours(Math.round(estimatedHours * 100.0) / 100.0);
        dto.setEstimatedDurationMinutes(estimatedMinutes);

        LocalDateTime completionTime = startTime.plusMinutes(estimatedMinutes);
        dto.setEstimatedCompletionTime(completionTime);

        // Calculate Estimated Cost: Cost = Energy Required * tariff per kWh
        double tariffRate = tariffDao.getRateForPower(point.getChargerPowerKw());
        double estimatedCost = Math.round(energyRequiredKwh * tariffRate * 100.0) / 100.0;
        dto.setEstimatedCost(estimatedCost);

        List<String> matchReasons = new ArrayList<>();
        matchReasons.add("Connector matched (" + point.getConnectorType() + ")");
        matchReasons.add("Power delivery: " + point.getChargerPowerKw() + " kW (@ ₹" + tariffRate + "/kWh)");

        boolean eligible = true;
        String rejectionReason = null;

        // Check 1: Charger Maintenance / Inactive
        if ("MAINTENANCE".equalsIgnoreCase(point.getStatus())) {
            eligible = false;
            rejectionReason = "Charger is currently under maintenance";
        }

        // Check 2: Interval overlap reservation conflict
        boolean hasConflict = reservationDao.hasConflict(point.getPointId(), startTime, completionTime, null);
        if (hasConflict) {
            eligible = false;
            rejectionReason = "Schedule conflict with an existing reservation in this time slot";
        } else {
            matchReasons.add("No reservation conflict");
        }

        // Check 3: Campus EV Load Check
        double projectedLoad = currentLoad + point.getChargerPowerKw();
        dto.setCampusLoadAfterAllocationKw(projectedLoad);
        if (projectedLoad > maxLoad) {
            eligible = false;
            rejectionReason = String.format("Exceeds campus maximum load capacity (Projected: %.1f kW > Max: %.1f kW)", projectedLoad, maxLoad);
        } else {
            matchReasons.add(String.format("Within campus load ceiling (Projected: %.1f / %.1f kW)", projectedLoad, maxLoad));
        }

        // Check 4: Departure Constraint
        boolean completesBeforeDeparture = (departureTime == null) || (!completionTime.isAfter(departureTime));
        if (!completesBeforeDeparture) {
            matchReasons.add("Warning: Estimated completion is after requested departure time");
        } else {
            matchReasons.add("Completes before requested departure");
        }

        // ==========================================
        // 5-FACTOR TRANSPARENT SCORING (Out of 100)
        // ==========================================
        
        // 1. Availability suitability (30 points)
        double availabilityScore = 0.0;
        if (eligible && "AVAILABLE".equalsIgnoreCase(point.getStatus())) {
            availabilityScore = 30.0;
        } else if (eligible && "OCCUPIED".equalsIgnoreCase(point.getStatus()) && !hasConflict) {
            availabilityScore = 15.0;
        }
        dto.setAvailabilityScore(availabilityScore);

        // 2. Waiting Time suitability (25 points)
        int waitMins = "AVAILABLE".equalsIgnoreCase(point.getStatus()) ? 0 : 15;
        dto.setEstimatedWaitMinutes(waitMins);
        double waitScore = eligible ? Math.max(0.0, 25.0 - (waitMins * 0.5)) : 0.0;
        dto.setWaitTimeScore(waitScore);

        // 3. Campus Load Efficiency (20 points)
        double loadScore = 0.0;
        if (eligible) {
            if (projectedLoad <= maxLoad * 0.80) {
                loadScore = 20.0;
            } else if (projectedLoad <= maxLoad) {
                loadScore = 14.0;
            }
        }
        dto.setLoadEfficiencyScore(loadScore);

        // 4. Preferred Location Suitability (15 points)
        double locationScore = 0.0;
        if (req.getPreferredLocation() != null && !req.getPreferredLocation().trim().isEmpty()) {
            if (point.getCampusLocation().equalsIgnoreCase(req.getPreferredLocation().trim()) ||
                point.getStationName().toLowerCase().contains(req.getPreferredLocation().toLowerCase().trim())) {
                locationScore = 15.0;
                matchReasons.add("Matches preferred campus location: " + req.getPreferredLocation());
            } else {
                locationScore = 6.0;
            }
        } else {
            locationScore = 12.0; // neutral preference
        }
        dto.setLocationScore(locationScore);

        // 5. Completion Before Departure (10 points)
        double departureScore = (eligible && completesBeforeDeparture) ? 10.0 : 2.0;
        dto.setDepartureScore(departureScore);

        double totalScore = eligible ? (availabilityScore + waitScore + loadScore + locationScore + departureScore) : 0.0;
        dto.setTotalScore(Math.round(totalScore * 10.0) / 10.0);
        dto.setEligible(eligible);
        dto.setRejectionReason(rejectionReason);
        dto.setMatchReasons(matchReasons);

        return dto;
    }

    private void validateRequest(RecommendationRequest req) {
        if (req.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Valid vehicle ID is required");
        }
        if (req.getCurrentBatteryPercent() < 0.0 || req.getCurrentBatteryPercent() > 100.0) {
            throw new IllegalArgumentException("Current battery percentage must be between 0% and 100%");
        }
        if (req.getTargetBatteryPercent() < 0.0 || req.getTargetBatteryPercent() > 100.0) {
            throw new IllegalArgumentException("Target battery percentage must be between 0% and 100%");
        }
        if (req.getTargetBatteryPercent() <= req.getCurrentBatteryPercent()) {
            throw new IllegalArgumentException("Target battery percentage must be greater than current battery percentage");
        }
        if (req.getExpectedDepartureTime() != null && req.getRequestedStartTime() != null) {
            if (req.getExpectedDepartureTime().isBefore(req.getRequestedStartTime())) {
                throw new IllegalArgumentException("Expected departure time cannot be before requested start time");
            }
        }
    }
}
