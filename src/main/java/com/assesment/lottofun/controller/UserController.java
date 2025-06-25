package com.assesment.lottofun.controller;

import com.assesment.lottofun.controller.response.ApiResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.controller.response.UserProfileResponse;
import com.assesment.lottofun.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@SecurityRequirement(name = "JWT")
@Tag(name = "User Management", description = "User profile and account management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Get user profile",
            description = "Retrieves the authenticated user's profile information"
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        String userEmail = getCurrentUserEmail();
        log.info("Profile request for user: {}", userEmail);

        UserProfileResponse profile = userService.profile(userEmail);

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/{drawId}/tickets")
    @Operation(
            summary = "Get user's tickets for specific draw",
            description = "Retrieves all tickets purchased by the user for a specific draw"
    )
    public ResponseEntity<ApiResponse<List<TicketDetailResponse>>> getUserTicketsForDraw(
            @Parameter(description = "Draw ID") @PathVariable Long drawId) {

        String userEmail = getCurrentUserEmail();
        List<TicketDetailResponse> tickets = userService.userTicketsForDraw(userEmail, drawId);
        return ResponseEntity.ok(ApiResponse.success("User tickets for draw retrieved successfully", tickets));
    }


    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}