package com.assesment.lottofun.service;

import com.assesment.lottofun.presentation.dto.common.PageResponse;
import com.assesment.lottofun.presentation.dto.response.ClaimTicketResponse;
import com.assesment.lottofun.presentation.dto.response.TicketDetailResponse;
import com.assesment.lottofun.presentation.dto.response.UserProfileResponse;
import com.assesment.lottofun.entity.*;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private Ticket sampleTicket;
    private Draw sampleDraw;

    @BeforeEach
    void setup() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .firstName("John")
                .lastName("Doe")
                .balance(BigDecimal.valueOf(1000))
                .build();

        sampleDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().plusHours(1))
                .status(DrawStatus.DRAW_OPEN)
                .build();

        sampleTicket = Ticket.builder()
                .id(1L)
                .ticketNumber("TKT-12345")
                .selectedNumbers("1,2,3,4,5")
                .status(TicketStatus.WON)
                .prizeAmount(BigDecimal.valueOf(100))
                .purchaseTimestamp(LocalDateTime.now())
                .draw(sampleDraw)
                .user(sampleUser)
                .build();
    }

    @Test
    void profile_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        String email = "test@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

        // When
        UserProfileResponse response = userService.profile(email);

        // Then
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void profile_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Given
        String email = "nonexistent@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.profile(email);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void claimTicket_ShouldReturnClaimResponse_WhenTicketIsClaimable() {
        // Given
        String email = "test@email.com";
        Long ticketId = 1L;
        
        sampleUser.setTickets(List.of(sampleTicket));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

        // When
        ClaimTicketResponse response = userService.claimTicket(email, ticketId);

        // Then
        assertNotNull(response);
        assertEquals(ticketId, response.getTicketId());
        assertEquals("TKT-12345", response.getTicketNumber());
        assertEquals(BigDecimal.valueOf(100), response.getClaimedAmount());
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(sampleUser);
    }

    @Test
    void claimTicket_ShouldThrowException_WhenTicketNotFound() {
        String email = "test@email.com";
        Long ticketId = 999L;

        sampleUser.setTickets(List.of()); // Empty tickets
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.claimTicket(email, ticketId);
        });

        assertEquals("Ticket not found for user", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void userAllTickets_ShouldReturnPagedTickets() {
        String email = "test@email.com";
        Pageable pageable = PageRequest.of(0, 10);

        Ticket ticket1 = Ticket.builder()
                .id(1L)
                .ticketNumber("TKT-1")
                .selectedNumbers("1,2,3,4,5")
                .status(TicketStatus.WAITING_FOR_DRAW)
                .purchaseTimestamp(LocalDateTime.of(2025, 6, 1, 12, 0))
                .draw(sampleDraw)
                .build();

        Ticket ticket2 = Ticket.builder()
                .id(2L)
                .ticketNumber("TKT-2")
                .selectedNumbers("6,7,8,9,10")
                .status(TicketStatus.WON)
                .purchaseTimestamp(LocalDateTime.of(2025, 6, 2, 12, 0))
                .draw(sampleDraw)
                .build();

        sampleUser.setTickets(List.of(ticket1, ticket2));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

        PageResponse<TicketDetailResponse> result = userService.userAllTickets(email, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void winningTickets_ShouldReturnWinningTickets() {
        String email = "test@email.com";

        Ticket winningTicket = Ticket.builder()
                .id(1L)
                .ticketNumber("TKT-WINNER")
                .selectedNumbers("1,2,3,4,5")
                .status(TicketStatus.WON)
                .prizeAmount(BigDecimal.valueOf(1000))
                .purchaseTimestamp(LocalDateTime.now())
                .draw(sampleDraw)
                .build();

        sampleUser.setTickets(List.of(winningTicket));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

        List<TicketDetailResponse> result = userService.winningTickets(email);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TKT-WINNER", result.get(0).getTicketNumber());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        String email = "test@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

}
