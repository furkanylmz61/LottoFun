package com.assesment.lottofun.controller;

import com.assesment.lottofun.controller.response.ApiResponse;
import com.assesment.lottofun.controller.response.DrawResponse;
import com.assesment.lottofun.controller.response.PageResponse;
import com.assesment.lottofun.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/draw")
@RestController
@SecurityRequirement(name = "JWT")
public class DrawController {

    private final DrawService _drawService;

    public DrawController(DrawService drawService) {
        _drawService = drawService;
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get current active draw",
            description = "Retrieves information about the current active draw that accepts ticket purchases"
    )
    public ResponseEntity<ApiResponse<DrawResponse>> getCurrentDraw() {
        DrawResponse currentDraw = _drawService.currentActiveDraw();
        return ResponseEntity.ok(ApiResponse.success("Current draw retrieved successfully", currentDraw));
    }


    @GetMapping("/history")
    @Operation(
            summary = "Get draw history",
            description = "Retrieves paginated list of completed draws with results"
    )
    public ResponseEntity<ApiResponse<PageResponse<DrawResponse>>> getDrawHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {


        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DrawResponse> drawHistory = _drawService.completedDraw(pageable);

        return ResponseEntity.ok(ApiResponse.success("Draw history retrieved successfully", drawHistory));
    }



}
