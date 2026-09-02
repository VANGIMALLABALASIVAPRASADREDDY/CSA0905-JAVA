package com.smartcharge.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcharge.client.model.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    // ==========================================
    // AUTH
    // ==========================================
    public UserDto login(String email, String password) throws IOException, InterruptedException {
        Map<String, String> body = Map.of("email", email, "password", password);
        ApiResponse<UserDto> response = post("/auth/login", body, new TypeReference<ApiResponse<UserDto>>() {});
        if (!response.isSuccess() || response.getData() == null) {
            throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Authentication failed");
        }
        return response.getData();
    }

    // ==========================================
    // VEHICLES
    // ==========================================
    public List<VehicleDto> getVehiclesByUser(int userId) throws IOException, InterruptedException {
        ApiResponse<List<VehicleDto>> res = get("/vehicles/user/" + userId, new TypeReference<ApiResponse<List<VehicleDto>>>() {});
        return res.getData();
    }

    public List<VehicleDto> getAllVehicles() throws IOException, InterruptedException {
        ApiResponse<List<VehicleDto>> res = get("/vehicles", new TypeReference<ApiResponse<List<VehicleDto>>>() {});
        return res.getData();
    }

    public VehicleDto registerVehicle(VehicleDto vehicle) throws IOException, InterruptedException {
        ApiResponse<VehicleDto> res = post("/vehicles", vehicle, new TypeReference<ApiResponse<VehicleDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public VehicleDto updateVehicle(int vehicleId, VehicleDto vehicle) throws IOException, InterruptedException {
        ApiResponse<VehicleDto> res = put("/vehicles/" + vehicleId, vehicle, new TypeReference<ApiResponse<VehicleDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public void deleteVehicle(int vehicleId) throws IOException, InterruptedException {
        ApiResponse<Void> res = delete("/vehicles/" + vehicleId, new TypeReference<ApiResponse<Void>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
    }

    // ==========================================
    // STATIONS & CHARGING POINTS
    // ==========================================
    public List<StationDto> getAllStations() throws IOException, InterruptedException {
        ApiResponse<List<StationDto>> res = get("/stations", new TypeReference<ApiResponse<List<StationDto>>>() {});
        return res.getData();
    }

    public StationDto createStation(StationDto dto) throws IOException, InterruptedException {
        ApiResponse<StationDto> res = post("/stations", dto, new TypeReference<ApiResponse<StationDto>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
        return res.getData();
    }

    public StationDto updateStation(int id, StationDto dto) throws IOException, InterruptedException {
        ApiResponse<StationDto> res = put("/stations/" + id, dto, new TypeReference<ApiResponse<StationDto>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
        return res.getData();
    }

    public void deleteStation(int id) throws IOException, InterruptedException {
        ApiResponse<Void> res = delete("/stations/" + id, new TypeReference<ApiResponse<Void>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
    }

    public List<ChargingPointDto> getAllChargingPoints() throws IOException, InterruptedException {
        ApiResponse<List<ChargingPointDto>> res = get("/charging-points", new TypeReference<ApiResponse<List<ChargingPointDto>>>() {});
        return res.getData();
    }

    public ChargingPointDto createChargingPoint(ChargingPointDto dto) throws IOException, InterruptedException {
        ApiResponse<ChargingPointDto> res = post("/charging-points", dto, new TypeReference<ApiResponse<ChargingPointDto>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
        return res.getData();
    }

    public ChargingPointDto updateChargingPoint(int id, ChargingPointDto dto) throws IOException, InterruptedException {
        ApiResponse<ChargingPointDto> res = put("/charging-points/" + id, dto, new TypeReference<ApiResponse<ChargingPointDto>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
        return res.getData();
    }

    public void updatePointStatus(int id, String status) throws IOException, InterruptedException {
        Map<String, String> body = Map.of("status", status);
        ApiResponse<Void> res = patch("/charging-points/" + id + "/status", body, new TypeReference<ApiResponse<Void>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
    }

    public void deleteChargingPoint(int id) throws IOException, InterruptedException {
        ApiResponse<Void> res = delete("/charging-points/" + id, new TypeReference<ApiResponse<Void>>() {});
        if (!res.isSuccess()) throw new RuntimeException(res.getMessage());
    }

    // ==========================================
    // RECOMMENDATION ENGINE
    // ==========================================
    public RecommendationResponseDto getRecommendation(Map<String, Object> req) throws IOException, InterruptedException {
        ApiResponse<RecommendationResponseDto> res = post("/recommendations", req, new TypeReference<ApiResponse<RecommendationResponseDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    // ==========================================
    // RESERVATIONS
    // ==========================================
    public ReservationDto createReservation(Map<String, Object> req) throws IOException, InterruptedException {
        ApiResponse<ReservationDto> res = post("/reservations", req, new TypeReference<ApiResponse<ReservationDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public List<ReservationDto> getReservationsByUser(int userId) throws IOException, InterruptedException {
        ApiResponse<List<ReservationDto>> res = get("/reservations/user/" + userId, new TypeReference<ApiResponse<List<ReservationDto>>>() {});
        return res.getData();
    }

    public List<ReservationDto> getAllReservations() throws IOException, InterruptedException {
        ApiResponse<List<ReservationDto>> res = get("/reservations", new TypeReference<ApiResponse<List<ReservationDto>>>() {});
        return res.getData();
    }

    public void cancelReservation(int reservationId) throws IOException, InterruptedException {
        ApiResponse<Void> res = post("/reservations/" + reservationId + "/cancel", null, new TypeReference<ApiResponse<Void>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
    }

    // ==========================================
    // VIRTUAL QUEUE
    // ==========================================
    public QueueEntryDto joinQueue(Map<String, Object> req) throws IOException, InterruptedException {
        ApiResponse<QueueEntryDto> res = post("/queue", req, new TypeReference<ApiResponse<QueueEntryDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public List<QueueEntryDto> getActiveQueue() throws IOException, InterruptedException {
        ApiResponse<List<QueueEntryDto>> res = get("/queue", new TypeReference<ApiResponse<List<QueueEntryDto>>>() {});
        return res.getData();
    }

    public List<QueueEntryDto> getAllQueueEntries() throws IOException, InterruptedException {
        ApiResponse<List<QueueEntryDto>> res = get("/queue/all", new TypeReference<ApiResponse<List<QueueEntryDto>>>() {});
        return res.getData();
    }

    // ==========================================
    // CHARGING SESSIONS (CHECK-IN & CHECK-OUT)
    // ==========================================
    public ChargingSessionDto checkIn(Map<String, Object> req) throws IOException, InterruptedException {
        ApiResponse<ChargingSessionDto> res = post("/sessions/check-in", req, new TypeReference<ApiResponse<ChargingSessionDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public ChargingSessionDto checkOut(int sessionId, double finalBatteryPercent) throws IOException, InterruptedException {
        Map<String, Object> req = Map.of("sessionId", sessionId, "finalBatteryPercent", finalBatteryPercent);
        ApiResponse<ChargingSessionDto> res = post("/sessions/" + sessionId + "/check-out", req, new TypeReference<ApiResponse<ChargingSessionDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public List<ChargingSessionDto> getActiveSessions() throws IOException, InterruptedException {
        ApiResponse<List<ChargingSessionDto>> res = get("/sessions/active", new TypeReference<ApiResponse<List<ChargingSessionDto>>>() {});
        return res.getData();
    }

    public List<ChargingSessionDto> getAllSessions() throws IOException, InterruptedException {
        ApiResponse<List<ChargingSessionDto>> res = get("/sessions", new TypeReference<ApiResponse<List<ChargingSessionDto>>>() {});
        return res.getData();
    }

    public List<ChargingSessionDto> getSessionsByUser(int userId) throws IOException, InterruptedException {
        ApiResponse<List<ChargingSessionDto>> res = get("/sessions/user/" + userId, new TypeReference<ApiResponse<List<ChargingSessionDto>>>() {});
        return res.getData();
    }

    // ==========================================
    // PAYMENTS
    // ==========================================
    public PaymentDto processPayment(Map<String, Object> req) throws IOException, InterruptedException {
        ApiResponse<PaymentDto> res = post("/payments", req, new TypeReference<ApiResponse<PaymentDto>>() {});
        if (!res.isSuccess()) {
            throw new RuntimeException(res.getMessage());
        }
        return res.getData();
    }

    public List<PaymentDto> getAllPayments() throws IOException, InterruptedException {
        ApiResponse<List<PaymentDto>> res = get("/payments", new TypeReference<ApiResponse<List<PaymentDto>>>() {});
        return res.getData();
    }

    public List<PaymentDto> getPaymentsByUser(int userId) throws IOException, InterruptedException {
        ApiResponse<List<PaymentDto>> res = get("/payments/user/" + userId, new TypeReference<ApiResponse<List<PaymentDto>>>() {});
        return res.getData();
    }

    // ==========================================
    // DASHBOARD & REPORTS
    // ==========================================
    public DashboardMetricsDto getDashboardMetrics() throws IOException, InterruptedException {
        ApiResponse<DashboardMetricsDto> res = get("/dashboard", new TypeReference<ApiResponse<DashboardMetricsDto>>() {});
        return res.getData();
    }

    public List<StationUtilizationDto> getStationUtilization() throws IOException, InterruptedException {
        ApiResponse<List<StationUtilizationDto>> res = get("/reports/station-utilization", new TypeReference<ApiResponse<List<StationUtilizationDto>>>() {});
        return res.getData();
    }

    public EnergyReportDto getEnergyReport() throws IOException, InterruptedException {
        ApiResponse<EnergyReportDto> res = get("/reports/energy", new TypeReference<ApiResponse<EnergyReportDto>>() {});
        return res.getData();
    }

    public UsageReportDto getUsageReport() throws IOException, InterruptedException {
        ApiResponse<UsageReportDto> res = get("/reports/usage", new TypeReference<ApiResponse<UsageReportDto>>() {});
        return res.getData();
    }

    public SustainabilityReportDto getSustainabilityReport() throws IOException, InterruptedException {
        ApiResponse<SustainabilityReportDto> res = get("/reports/sustainability", new TypeReference<ApiResponse<SustainabilityReportDto>>() {});
        return res.getData();
    }

    public boolean checkHealth() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/health"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================
    // HTTP GENERIC HELPERS
    // ==========================================
    private <T> T get(String endpoint, TypeReference<T> typeRef) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, typeRef);
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new IOException("Unable to connect to SmartCharge backend. Check whether the backend is running on localhost:8080.", e);
        }
    }

    private <T> T post(String endpoint, Object body, TypeReference<T> typeRef) throws IOException, InterruptedException {
        try {
            String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, typeRef);
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new IOException("Unable to connect to SmartCharge backend. Check whether the backend is running on localhost:8080.", e);
        }
    }

    private <T> T put(String endpoint, Object body, TypeReference<T> typeRef) throws IOException, InterruptedException {
        try {
            String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, typeRef);
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new IOException("Unable to connect to SmartCharge backend. Check whether the backend is running on localhost:8080.", e);
        }
    }

    private <T> T patch(String endpoint, Object body, TypeReference<T> typeRef) throws IOException, InterruptedException {
        try {
            String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, typeRef);
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new IOException("Unable to connect to SmartCharge backend. Check whether the backend is running on localhost:8080.", e);
        }
    }

    private <T> T delete(String endpoint, TypeReference<T> typeRef) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, typeRef);
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new IOException("Unable to connect to SmartCharge backend. Check whether the backend is running on localhost:8080.", e);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, TypeReference<T> typeRef) throws IOException {
        String body = response.body();
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(body, typeRef);
        } else {
            try {
                ApiResponse<?> err = objectMapper.readValue(body, ApiResponse.class);
                throw new RuntimeException(err.getMessage() != null ? err.getMessage() : "Server returned status code: " + response.statusCode());
            } catch (Exception ignored) {
                throw new RuntimeException("Server error (" + response.statusCode() + "): " + body);
            }
        }
    }
}
