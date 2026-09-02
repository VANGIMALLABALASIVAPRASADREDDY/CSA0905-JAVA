package com.smartcharge.controller;

import com.smartcharge.dto.*;
import com.smartcharge.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Stored Procedure Report via CallableStatement
     */
    @GetMapping("/station-utilization")
    public ResponseEntity<ApiResponse<List<StationUtilizationDto>>> getStationUtilization() {
        return ResponseEntity.ok(ApiResponse.ok("Station utilization retrieved from stored procedure", reportService.getStationUtilization()));
    }

    @GetMapping("/energy")
    public ResponseEntity<ApiResponse<EnergyReportDto>> getEnergyReport() {
        return ResponseEntity.ok(ApiResponse.ok("Energy consumption report retrieved", reportService.getEnergyReport()));
    }

    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<UsageReportDto>> getUsageReport() {
        return ResponseEntity.ok(ApiResponse.ok("Usage analytics report retrieved", reportService.getUsageReport()));
    }

    @GetMapping("/sustainability")
    public ResponseEntity<ApiResponse<SustainabilityReportDto>> getSustainabilityReport() {
        return ResponseEntity.ok(ApiResponse.ok("Sustainability metrics retrieved", reportService.getSustainabilityReport()));
    }
}
