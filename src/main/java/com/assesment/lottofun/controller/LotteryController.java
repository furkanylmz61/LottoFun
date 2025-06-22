package com.assesment.lottofun.controller;

import com.assesment.lottofun.dto.request.TicketPurchaseRequest;
import com.assesment.lottofun.dto.response.ApiResponse;
import com.assesment.lottofun.dto.response.DrawResponse;
import com.assesment.lottofun.dto.response.PageResponse;
import com.assesment.lottofun.dto.response.TicketBasicResponse;
import com.assesment.lottofun.dto.response.TicketDetailResponse;
import com.assesment.lottofun.service.DrawService;
import com.assesment.lottofun.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Lottery", description = "Lottery game endpoints for ticket purchasing and draw management")
public class LotteryController {

    private final TicketService ticketService;
    private final DrawService drawService;

    @PostMapping("/tickets")
    @Operation(
            summary = "Purchase a lottery ticket",
            description = "Allows authenticated users to purchase a ticket by selecting 5 unique numbers between 1-49"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Ticket purchased successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid numbers or insufficient balance",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Duplicate ticket (same numbers for same draw)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<TicketBasicResponse>> purchaseTicket(
            @Valid @RequestBody TicketPurchaseRequest request) {

        String userEmail = getCurrentUserEmail();
        log.info("Ticket purchase request from user: {} with numbers: {}", userEmail, request.getSelectedNumbers());

        TicketBasicResponse ticket = ticketService.purchaseTicket(userEmail, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket purchased successfully", ticket));
    }

    @GetMapping("/tickets")
    @Operation(
            summary = "Get user's tickets",
            description = "Retrieves paginated list of tickets purchased by the authenticated user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tickets retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<TicketBasicResponse>>> getUserTickets(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        String userEmail = getCurrentUserEmail();
        log.info("Getting tickets for user: {} - page: {}, size: {}", userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<TicketBasicResponse> tickets = ticketService.getUserTickets(userEmail, pageable);

        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }

    @GetMapping("/tickets/{ticketId}")
    @Operation(
            summary = "Get ticket details",
            description = "Retrieves detailed information about a specific ticket including draw results if available"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ticket details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Ticket does not belong to current user",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetails(
            @Parameter(description = "Ticket ID") @PathVariable Long ticketId) {

        String userEmail = getCurrentUserEmail();
        log.info("Getting ticket details for user: {} and ticket: {}", userEmail, ticketId);

        TicketDetailResponse ticket = ticketService.getTicketDetails(userEmail, ticketId);

        return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully", ticket));
    }

    @GetMapping("/draws/current")
    @Operation(
            summary = "Get current active draw",
            description = "Retrieves information about the current active draw that accepts ticket purchases"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current draw retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DrawResponse>> getCurrentDraw() {
        log.info("Getting current active draw");

        DrawResponse currentDraw = drawService.getCurrentActiveDrawResponse();

        return ResponseEntity.ok(ApiResponse.success("Current draw retrieved successfully", currentDraw));
    }

    @GetMapping("/draws/history")
    @Operation(
            summary = "Get draw history",
            description = "Retrieves paginated list of completed draws with results"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Draw history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<DrawResponse>>> getDrawHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        log.info("Getting draw history - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DrawResponse> drawHistory = drawService.getCompletedDraws(pageable);

        return ResponseEntity.ok(ApiResponse.success("Draw history retrieved successfully", drawHistory));
    }

    @GetMapping("/draws/{drawId}/tickets")
    @Operation(
            summary = "Get user's tickets for specific draw",
            description = "Retrieves all tickets purchased by the user for a specific draw"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User tickets for draw retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Draw not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<TicketDetailResponse>>> getUserTicketsForDraw(
            @Parameter(description = "Draw ID") @PathVariable Long drawId) {

        String userEmail = getCurrentUserEmail();
        log.info("Getting tickets for user: {} and draw: {}", userEmail, drawId);

        List<TicketDetailResponse> tickets = ticketService.getUserTicketsForDraw(userEmail, drawId);

        return ResponseEntity.ok(ApiResponse.success("User tickets for draw retrieved successfully", tickets));
    }

    @GetMapping("/numbers/validate")
    @Operation(
            summary = "Validate lottery numbers",
            description = "Validates if the provided numbers are valid for lottery ticket purchase"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Numbers validation result",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Boolean>> validateNumbers(
            @Parameter(description = "Comma-separated list of 5 numbers between 1-49")
            @RequestParam String numbers) {

        log.info("Validating numbers: {}", numbers);

        try {
            String[] numberArray = numbers.split(",");
            List<Integer> numberList = List.of(numberArray).stream()
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            String userEmail = getCurrentUserEmail();
            boolean canPurchase = ticketService.canUserPurchaseTicket(userEmail, numberList);

            return ResponseEntity.ok(ApiResponse.success("Numbers validated successfully", canPurchase));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("Numbers validation failed", false));
        }
    }


    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}