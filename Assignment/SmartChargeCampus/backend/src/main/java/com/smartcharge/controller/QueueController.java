package com.smartcharge.controller;

import com.smartcharge.dto.ApiResponse;
import com.smartcharge.dto.QueueRequest;
import com.smartcharge.model.QueueEntry;
import com.smartcharge.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QueueEntry>> joinQueue(@RequestBody QueueRequest request) {
        QueueEntry entry = queueService.joinQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Successfully joined virtual queue", entry));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QueueEntry>>> getActiveQueue() {
        return ResponseEntity.ok(ApiResponse.ok(queueService.getActiveQueue()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<QueueEntry>>> getAllQueueEntries() {
        return ResponseEntity.ok(ApiResponse.ok(queueService.getAllQueueEntries()));
    }
}
