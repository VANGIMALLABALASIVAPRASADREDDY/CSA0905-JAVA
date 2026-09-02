    package com.smartcharge.controller;

    import com.smartcharge.dto.ApiResponse;
    import com.smartcharge.dto.LoginRequest;
    import com.smartcharge.dto.LoginResponse;
    import com.smartcharge.service.AuthService;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/auth")
    @CrossOrigin(origins = "*")
    public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
            this.authService = authService;
        }

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
            LoginResponse response = authService.authenticate(request);
            return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
        }
    }
