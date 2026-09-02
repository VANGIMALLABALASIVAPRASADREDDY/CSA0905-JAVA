package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidateScoreDto {
    private int pointId;
    private String pointName;
    private int stationId;
    private String stationName;
    private String campusLocation;
    private double chargerPowerKw;
    private String connectorType;
    private String status;
    private double totalScore;
    private double availabilityScore;
    private double waitTimeScore;
    private double loadEfficiencyScore;
    private double locationScore;
    private double departureScore;
    private double requiredEnergyKwh;
    private double estimatedDurationHours;
    private int estimatedDurationMinutes;
    private double estimatedCost;
    private int estimatedWaitMinutes;
    private LocalDateTime estimatedCompletionTime;
    private double campusLoadAfterAllocationKw;
    private boolean eligible;
    private String rejectionReason;
    private List<String> matchReasons;

    public CandidateScoreDto() {}

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCampusLocation() {
        return campusLocation;
    }

    public void setCampusLocation(String campusLocation) {
        this.campusLocation = campusLocation;
    }

    public double getChargerPowerKw() {
        return chargerPowerKw;
    }

    public void setChargerPowerKw(double chargerPowerKw) {
        this.chargerPowerKw = chargerPowerKw;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getAvailabilityScore() {
        return availabilityScore;
    }

    public void setAvailabilityScore(double availabilityScore) {
        this.availabilityScore = availabilityScore;
    }

    public double getWaitTimeScore() {
        return waitTimeScore;
    }

    public void setWaitTimeScore(double waitTimeScore) {
        this.waitTimeScore = waitTimeScore;
    }

    public double getLoadEfficiencyScore() {
        return loadEfficiencyScore;
    }

    public void setLoadEfficiencyScore(double loadEfficiencyScore) {
        this.loadEfficiencyScore = loadEfficiencyScore;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

    public double getDepartureScore() {
        return departureScore;
    }

    public void setDepartureScore(double departureScore) {
        this.departureScore = departureScore;
    }

    public double getRequiredEnergyKwh() {
        return requiredEnergyKwh;
    }

    public void setRequiredEnergyKwh(double requiredEnergyKwh) {
        this.requiredEnergyKwh = requiredEnergyKwh;
    }

    public double getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(double estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public LocalDateTime getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    public double getCampusLoadAfterAllocationKw() {
        return campusLoadAfterAllocationKw;
    }

    public void setCampusLoadAfterAllocationKw(double campusLoadAfterAllocationKw) {
        this.campusLoadAfterAllocationKw = campusLoadAfterAllocationKw;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }
}
