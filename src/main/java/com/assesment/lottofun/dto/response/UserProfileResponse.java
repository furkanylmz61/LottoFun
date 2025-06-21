package com.assesment.lottofun.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private String role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}