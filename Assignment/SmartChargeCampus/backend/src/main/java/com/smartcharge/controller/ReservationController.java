package com.smartcharge.controller;

import com.smartcharge.dto.ApiResponse;
import com.smartcharge.dto.ReservationRequest;
import com.smartcharge.model.Reservation;
import com.smartcharge.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Reservation>> createReservation(@RequestBody ReservationRequest request) {
        Reservation res = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Reservation created successfully", res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Reservation>>> getAllReservations() {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getAllReservations()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Reservation>>> getReservationsByUser(@PathVariable int userId) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getReservationsByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Reservation>> getReservationById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getReservationById(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable int id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.ok("Reservation cancelled successfully", null));
    }
}
