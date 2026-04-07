package com.oneness.dine.auth.controller;

import com.oneness.dine.auth.dto.LoginRequest;
import com.oneness.dine.auth.dto.LoginResponse;
import com.oneness.dine.auth.dto.UserDto;
import com.oneness.dine.auth.service.AuthService;
import com.oneness.dine.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/test-hash")
    public ResponseEntity<String> testHash(
            @RequestParam String password) {
        return ResponseEntity.ok(passwordEncoder.encode(password));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            Principal principal) {
        UserDto user = authService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/pin-login")
    public ResponseEntity<ApiResponse<LoginResponse>> pinLogin(
            @RequestBody Map<String, String> request) {
        LoginResponse response = authService.pinLogin(
                request.get("pinCode"),
                request.get("terminalId")
        );
        return ResponseEntity.ok(
                ApiResponse.success(response, "PIN login successful"));
    }
}