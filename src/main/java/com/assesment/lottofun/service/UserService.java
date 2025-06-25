package com.assesment.lottofun.service;

import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.controller.response.UserProfileResponse;
import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.TicketRepository;
import com.assesment.lottofun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse profile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public User addBalance(String email, BigDecimal amount) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.addBalance(amount);
        userRepository.saveAll(List.of(user));
        return user;
    }

    @Transactional
    public void deductBalance(User user, BigDecimal amount) {
        user.deductBalance(amount);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<TicketDetailResponse> userTicketsForDraw(String userEmail, Long drawId) {

        User user = getUserByEmail(userEmail);
        return user.getTickets().stream()
                .filter(f -> f.getDraw().getId().equals(drawId))
                .map(TicketDetailResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }
}