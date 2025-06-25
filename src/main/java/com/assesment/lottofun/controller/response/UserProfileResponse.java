package com.assesment.lottofun.controller.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public static UserProfileResponse fromEntity(com.assesment.lottofun.entity.User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBalance(user.getBalance());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}