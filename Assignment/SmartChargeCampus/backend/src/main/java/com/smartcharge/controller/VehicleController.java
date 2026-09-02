package com.smartcharge.controller;

import com.smartcharge.dto.ApiResponse;
import com.smartcharge.model.Vehicle;
import com.smartcharge.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Vehicle>>> getAllVehicles() {
        return ResponseEntity.ok(ApiResponse.ok(vehicleService.getAllVehicles()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getVehiclesByUser(@PathVariable int userId) {
        return ResponseEntity.ok(ApiResponse.ok(vehicleService.getVehiclesByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicleById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.ok(vehicleService.getVehicleById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> registerVehicle(@RequestBody Vehicle vehicle) {
        Vehicle saved = vehicleService.registerVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Vehicle registered successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(@PathVariable int id, @RequestBody Vehicle vehicle) {
        Vehicle updated = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable int id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle deleted successfully", null));
    }
}
