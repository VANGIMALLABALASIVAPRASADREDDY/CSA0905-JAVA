package com.smartcharge.controller;

import com.smartcharge.dto.ApiResponse;
import com.smartcharge.dto.RecommendationRequest;
import com.smartcharge.dto.RecommendationResponse;
import com.smartcharge.service.ChargingRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final ChargingRecommendationService recommendationService;

    public RecommendationController(ChargingRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendation(@RequestBody RecommendationRequest request) {
        RecommendationResponse response = recommendationService.recommendBestCharger(request);
        return ResponseEntity.ok(ApiResponse.ok("Recommendation evaluated successfully", response));
    }
}
