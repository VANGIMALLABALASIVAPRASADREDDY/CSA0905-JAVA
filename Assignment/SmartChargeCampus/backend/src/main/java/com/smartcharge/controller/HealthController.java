package com.smartcharge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "SmartCharge backend is running");
        status.put("timestamp", System.currentTimeMillis());

        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            status.put("database", valid ? "CONNECTED (Local MySQL smartcharge_campus)" : "ERROR");
        } catch (SQLException e) {
            status.put("database", "DISCONNECTED: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }
}
