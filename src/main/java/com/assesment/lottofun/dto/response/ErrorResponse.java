package com.assesment.lottofun.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private boolean success = false;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorResponse(String message, String errorCode, String path) {
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}