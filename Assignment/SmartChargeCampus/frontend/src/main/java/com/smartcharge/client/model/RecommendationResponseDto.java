package com.smartcharge.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponseDto {
    private boolean matchFound;
    private CandidateScoreDto bestCharger;
    private List<CandidateScoreDto> rankedCandidates;
    private double currentCampusLoadKw;
    private double maxCampusLoadKw;
    private boolean virtualQueueRecommended;
    private String message;

    public RecommendationResponseDto() {}

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
