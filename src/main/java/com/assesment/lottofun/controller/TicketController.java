package com.assesment.lottofun.controller;

import com.assesment.lottofun.controller.request.TicketPurchaseRequest;
import com.assesment.lottofun.controller.response.ApiResponse;
import com.assesment.lottofun.controller.response.TicketBasicResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/ticket")
@RestController
@SecurityRequirement(name = "JWT")
public class TicketController {

    private final TicketService _ticketService;

    public TicketController(TicketService ticketService) {
        _ticketService = ticketService;
    }

    @PostMapping("/purchase")
    @Operation(
            summary = "Purchase a lottery ticket",
            description = "Allows authenticated users to purchase a ticket by selecting 5 unique numbers between 1-49"
    )
    public ResponseEntity<ApiResponse<TicketBasicResponse>> purchaseTicket(
            @Valid @RequestBody TicketPurchaseRequest request) {

        String userEmail = getCurrentUserEmail();
        TicketBasicResponse ticket = _ticketService.purchase(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket purchased successfully", ticket));
    }


    @GetMapping("/{ticketId}")
    @Operation(
            summary = "Get ticket details",
            description = "Retrieves detailed information about a specific ticket including draw results if available"
    )
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetails(
            @Parameter(description = "Ticket ID") @PathVariable Long ticketId) {

        String userEmail = getCurrentUserEmail();
        TicketDetailResponse ticket = _ticketService.ticketDetail(userEmail, ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully", ticket));
    }


    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
