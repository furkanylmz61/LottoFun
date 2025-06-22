package com.assesment.lottofun.controller;

import com.assesment.lottofun.dto.response.ApiResponse;
import com.assesment.lottofun.dto.response.BalanceResponse;
import com.assesment.lottofun.dto.response.UserProfileResponse;
import com.assesment.lottofun.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User Management", description = "User profile and account management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(
            summary = "Get user profile",
            description = "Retrieves the authenticated user's profile information"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized access",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        String userEmail = getCurrentUserEmail();
        log.info("Profile request for user: {}", userEmail);

        UserProfileResponse profile = userService.getUserProfile(userEmail);

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/balance")
    @Operation(
            summary = "Get user balance",
            description = "Retrieves the authenticated user's current account balance"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Balance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized access",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance() {
        String userEmail = getCurrentUserEmail();
        log.info("Balance request for user: {}", userEmail);

        BalanceResponse balance = userService.getUserBalance(userEmail);

        return ResponseEntity.ok(ApiResponse.success("Balance retrieved successfully", balance));
    }


    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}