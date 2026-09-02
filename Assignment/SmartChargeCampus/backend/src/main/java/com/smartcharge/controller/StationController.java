package com.smartcharge.controller;

import com.smartcharge.dao.StationDao;
import com.smartcharge.dto.ApiResponse;
import com.smartcharge.model.ChargingStation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class StationController {

    private final StationDao stationDao;

    public StationController(StationDao stationDao) {
        this.stationDao = stationDao;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChargingStation>>> getAllStations() {
        return ResponseEntity.ok(ApiResponse.ok(stationDao.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargingStation>> getStationById(@PathVariable int id) {
        ChargingStation s = stationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found for ID: " + id));
        return ResponseEntity.ok(ApiResponse.ok(s));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChargingStation>> createStation(@RequestBody ChargingStation station) {
        if (station.getStationName() == null || station.getStationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Station name is required");
        }
        if (station.getCampusLocation() == null || station.getCampusLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Campus location is required");
        }
        ChargingStation saved = stationDao.insert(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Station created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargingStation>> updateStation(@PathVariable int id, @RequestBody ChargingStation station) {
        station.setStationId(id);
        boolean updated = stationDao.update(station);
        if (!updated) {
            throw new IllegalArgumentException("Failed to update station ID: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Station updated successfully", stationDao.findById(id).orElse(station)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStation(@PathVariable int id) {
        boolean deleted = stationDao.deleteById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Failed to delete station ID: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok("Station deleted successfully", null));
    }
}
