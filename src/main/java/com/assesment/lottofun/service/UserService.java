package com.assesment.lottofun.service;

import com.assesment.lottofun.dto.response.BalanceResponse;
import com.assesment.lottofun.dto.response.UserProfileResponse;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        log.debug("Getting profile for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBalance(user.getBalance());
        response.setRole("USER");
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getUpdatedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getUserBalance(String email) {
        log.debug("Getting balance for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return new BalanceResponse(user.getBalance());
    }

    @Transactional
    public User updateUserBalance(String email, BigDecimal amount, String operation) {
        log.debug("Updating balance for user: {} with amount: {} operation: {}", email, amount, operation);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        switch (operation.toLowerCase()) {
            case "add":
                user.addBalance(amount);
                break;
            case "deduct":
                user.deductBalance(amount);
                break;
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public boolean hasUser(String email) {
        return userRepository.existsByEmail(email);
    }
}