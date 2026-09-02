package com.smartcharge.controller;

import com.smartcharge.dto.ApiResponse;
import com.smartcharge.dto.CheckInRequest;
import com.smartcharge.dto.CheckOutRequest;
import com.smartcharge.model.ChargingSession;
import com.smartcharge.service.ChargingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class ChargingSessionController {

    private final ChargingSessionService sessionService;

    public ChargingSessionController(ChargingSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<ChargingSession>> checkIn(@RequestBody CheckInRequest request) {
        ChargingSession session = sessionService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Checked in successfully. Charging started.", session));
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<ApiResponse<ChargingSession>> checkOut(@PathVariable int id, @RequestBody(required = false) CheckOutRequest request) {
        if (request == null) {
            request = new CheckOutRequest(id, 0.0);
        } else {
            request.setSessionId(id);
        }
        ChargingSession session = sessionService.checkOut(request);
        return ResponseEntity.ok(ApiResponse.ok("Checked out successfully. Payment calculation generated.", session));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ChargingSession>>> getActiveSessions() {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getActiveSessions()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChargingSession>>> getAllSessions() {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getAllSessions()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ChargingSession>>> getSessionsByUser(@PathVariable int userId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getSessionsByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargingSession>> getSessionById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getSessionById(id)));
    }
}
