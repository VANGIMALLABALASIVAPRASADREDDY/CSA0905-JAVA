package com.smartcharge.dto;

import java.util.List;

public class RecommendationResponse {
    private boolean matchFound;
    private CandidateScoreDto bestCharger;
    private List<CandidateScoreDto> rankedCandidates;
    private double currentCampusLoadKw;
    private double maxCampusLoadKw;
    private boolean virtualQueueRecommended;
    private String message;

    public RecommendationResponse() {}

    public boolean isMatchFound() {
        return matchFound;
    }

    public void setMatchFound(boolean matchFound) {
        this.matchFound = matchFound;
    }

    public CandidateScoreDto getBestCharger() {
        return bestCharger;
    }

    public void setBestCharger(CandidateScoreDto bestCharger) {
        this.bestCharger = bestCharger;
    }

    public List<CandidateScoreDto> getRankedCandidates() {
        return rankedCandidates;
    }

    public void setRankedCandidates(List<CandidateScoreDto> rankedCandidates) {
        this.rankedCandidates = rankedCandidates;
    }

    public double getCurrentCampusLoadKw() {
        return currentCampusLoadKw;
    }

    public void setCurrentCampusLoadKw(double currentCampusLoadKw) {
        this.currentCampusLoadKw = currentCampusLoadKw;
    }

    public double getMaxCampusLoadKw() {
        return maxCampusLoadKw;
    }

    public void setMaxCampusLoadKw(double maxCampusLoadKw) {
        this.maxCampusLoadKw = maxCampusLoadKw;
    }

    public boolean isVirtualQueueRecommended() {
        return virtualQueueRecommended;
    }

    public void setVirtualQueueRecommended(boolean virtualQueueRecommended) {
        this.virtualQueueRecommended = virtualQueueRecommended;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
