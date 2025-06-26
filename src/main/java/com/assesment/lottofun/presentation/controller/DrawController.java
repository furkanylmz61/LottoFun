package com.assesment.lottofun.presentation.controller;

import com.assesment.lottofun.presentation.dto.common.ApiResponse;
import com.assesment.lottofun.presentation.dto.response.DrawResponse;
import com.assesment.lottofun.presentation.dto.common.PageResponse;
import com.assesment.lottofun.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/draw")
@RestController
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;


    @GetMapping("/active")
    @Operation(
            summary = "Get current active draw",
            description = "Retrieves information about the current active draw that accepts ticket purchases"
    )
    public ResponseEntity<ApiResponse<DrawResponse>> getCurrentDraw() {
        return ResponseEntity.ok(ApiResponse.success("Current draw retrieved successfully",
                DrawResponse.fromEntity(drawService.getActiveDraw())));
    }


    @GetMapping("/history")
    @Operation(
            summary = "Get draw history",
            description = "Retrieves paginated list of completed draws with results"
    )
    public ResponseEntity<ApiResponse<PageResponse<DrawResponse>>> getDrawHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction) {


        PageResponse<DrawResponse> drawHistory = drawService.filter(page, size, direction);

        return ResponseEntity.ok(ApiResponse.success("Draw history retrieved successfully", drawHistory));
    }


}
