package com.smartcharge.controller;

import com.smartcharge.dto.ApiResponse;
import com.smartcharge.dto.DashboardMetricsDto;
import com.smartcharge.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardMetricsDto>> getDashboardMetrics() {
        return ResponseEntity.ok(ApiResponse.ok("Live dashboard metrics retrieved from MySQL", dashboardService.getDashboardMetrics()));
    }
}
