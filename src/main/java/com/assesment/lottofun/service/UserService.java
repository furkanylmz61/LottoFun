package com.assesment.lottofun.service;

import com.assesment.lottofun.controller.response.ClaimTicketResponse;
import com.assesment.lottofun.controller.response.PageResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.controller.response.UserProfileResponse;
import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.TicketRepository;
import com.assesment.lottofun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse profile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserProfileResponse.fromEntity(user);
    }


    @Transactional(readOnly = true)
    public List<TicketDetailResponse> userTicketsForDraw(String userEmail, Long drawId) {
        User user = userRepository.findByEmailWithTicketsForDraw(userEmail, drawId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Ticket> tickets = user.getTicketsForDraw(drawId);

        return tickets.stream()
                .map(TicketDetailResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketDetailResponse> userAllTickets(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmailWithTickets(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Ticket> allTickets = user.getAllTicketsSortedByDate();

        int totalElements = allTickets.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), totalElements);

        List<Ticket> pageContent = start < totalElements ?
                allTickets.subList(start, end) : List.of();

        List<TicketDetailResponse> responseContent = pageContent.stream()
                .map(TicketDetailResponse::fromEntity)
                .collect(Collectors.toList());

        Page<TicketDetailResponse> page = new PageImpl<>(
                responseContent,
                pageable,
                totalElements
        );

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public List<TicketDetailResponse> winningTickets(String userEmail) {
        User user = userRepository.findByEmailWithTickets(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Ticket> winningTickets = user.getWinningTickets();

        return winningTickets.stream()
                .map(TicketDetailResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketDetailResponse> claimableTickets(String userEmail) {
        User user = userRepository.findByEmailWithTickets(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Ticket> claimableTickets = user.getClaimableTickets();

        return claimableTickets.stream()
                .map(TicketDetailResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimTicketResponse claimTicket(String userEmail, Long ticketId) {
        User user = userRepository.findByEmailWithTickets(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Ticket ticket = user.getTicketById(ticketId);
        if (ticket == null) {
            throw new ResourceNotFoundException("Ticket not found or doesn't belong to user");
        }

        BigDecimal claimedAmount = ticket.getPrizeAmount();
        String ticketNumber = ticket.getTicketNumber();

        user.claimTicket(ticketId);

        userRepository.save(user);

        log.info("User {} claimed ticket {} for amount {}", userEmail, ticketId, claimedAmount);

        return ClaimTicketResponse.create(
                ticketId,
                ticketNumber,
                claimedAmount,
                ticket.getClaimedAt(),
                user.getBalance()
        );
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