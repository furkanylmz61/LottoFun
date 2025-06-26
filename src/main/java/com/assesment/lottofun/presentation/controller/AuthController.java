package com.assesment.lottofun.presentation.controller;

import com.assesment.lottofun.presentation.dto.request.AuthRequest;
import com.assesment.lottofun.presentation.dto.request.RegisterRequest;
import com.assesment.lottofun.presentation.dto.common.ApiResponse;
import com.assesment.lottofun.presentation.dto.response.AuthResponse;
import com.assesment.lottofun.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService _authService;


    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with predefined balance and returns JWT token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = _authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user credentials and returns JWT token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = _authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", authResponse));
    }
}