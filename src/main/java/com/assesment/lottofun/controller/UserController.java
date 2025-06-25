package com.assesment.lottofun.controller;

import com.assesment.lottofun.controller.response.ApiResponse;
import com.assesment.lottofun.controller.response.ClaimTicketResponse;
import com.assesment.lottofun.controller.response.PageResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.controller.response.UserProfileResponse;
import com.assesment.lottofun.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/tickets")
    @Operation(
            summary = "Get all user tickets with pagination",
            description = "Retrieves paginated list of all tickets purchased by the user"
    )
    public ResponseEntity<ApiResponse<PageResponse<TicketDetailResponse>>> getAllUserTickets(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        String userEmail = getCurrentUserEmail();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<TicketDetailResponse> tickets = userService.userAllTickets(userEmail, pageable);

        return ResponseEntity.ok(ApiResponse.success("User tickets retrieved successfully", tickets));
    }

    @GetMapping("/tickets/won")
    @Operation(
            summary = "Get user's winning tickets",
            description = "Retrieves all winning tickets that can be claimed by the user"
    )
    public ResponseEntity<ApiResponse<List<TicketDetailResponse>>> getWinningTickets() {
        String userEmail = getCurrentUserEmail();
        List<TicketDetailResponse> winningTickets = userService.winningTickets(userEmail);

        return ResponseEntity.ok(ApiResponse.success("Winning tickets retrieved successfully", winningTickets));
    }

    @PostMapping("/tickets/{ticketId}/claim")
    @Operation(
            summary = "Claim prize for winning ticket",
            description = "Claims the prize for a winning ticket and adds the amount to user's balance"
    )
    public ResponseEntity<ApiResponse<ClaimTicketResponse>> claimTicket(
            @Parameter(description = "Ticket ID to claim") @PathVariable Long ticketId) {

        String userEmail = getCurrentUserEmail();
        ClaimTicketResponse claimedTicket = userService.claimTicket(userEmail, ticketId);

        return ResponseEntity.ok(ApiResponse.success("Ticket claimed successfully", claimedTicket));
    }

    @GetMapping("/tickets/claimable")
    @Operation(
            summary = "Get claimable tickets",
            description = "Retrieves all tickets that have won prizes and can be claimed"
    )
    public ResponseEntity<ApiResponse<List<TicketDetailResponse>>> getClaimableTickets() {
        String userEmail = getCurrentUserEmail();
        List<TicketDetailResponse> claimableTickets = userService.claimableTickets(userEmail);

        return ResponseEntity.ok(ApiResponse.success("Claimable tickets retrieved successfully", claimableTickets));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}