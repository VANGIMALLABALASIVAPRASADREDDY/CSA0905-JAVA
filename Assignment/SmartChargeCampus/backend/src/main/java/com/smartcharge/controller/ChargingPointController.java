package com.smartcharge.controller;

import com.smartcharge.dao.ChargingPointDao;
import com.smartcharge.dto.ApiResponse;
import com.smartcharge.model.ChargingPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charging-points")
@CrossOrigin(origins = "*")
public class ChargingPointController {

    private final ChargingPointDao pointDao;

    public ChargingPointController(ChargingPointDao pointDao) {
        this.pointDao = pointDao;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChargingPoint>>> getAllPoints() {
        return ResponseEntity.ok(ApiResponse.ok(pointDao.findAll()));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<ApiResponse<List<ChargingPoint>>> getPointsByStation(@PathVariable int stationId) {
        return ResponseEntity.ok(ApiResponse.ok(pointDao.findByStationId(stationId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargingPoint>> getPointById(@PathVariable int id) {
        ChargingPoint p = pointDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Charging point not found for ID: " + id));
        return ResponseEntity.ok(ApiResponse.ok(p));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChargingPoint>> createPoint(@RequestBody ChargingPoint point) {
        ChargingPoint saved = pointDao.insert(point);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Charging point created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargingPoint>> updatePoint(@PathVariable int id, @RequestBody ChargingPoint point) {
        point.setPointId(id);
        boolean updated = pointDao.update(point);
        if (!updated) {
            throw new IllegalArgumentException("Failed to update charging point ID: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Charging point updated successfully", pointDao.findById(id).orElse(point)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updatePointStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status string is required");
        }
        boolean updated = pointDao.updateStatus(id, status.trim().toUpperCase());
        if (!updated) {
            throw new IllegalArgumentException("Failed to update status for point ID: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + status, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePoint(@PathVariable int id) {
        boolean deleted = pointDao.deleteById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Failed to delete charging point ID: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Charging point deleted successfully", null));
    }
}
