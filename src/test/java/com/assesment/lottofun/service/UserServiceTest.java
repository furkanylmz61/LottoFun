package com.assesment.lottofun.service;

import com.assesment.lottofun.controller.response.PageResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.entity.*;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProfile_userExists_returnProfileResponse() {
        String email = "furkan@email.com";
        User user = User.builder()
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .password("pass")
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var response = userService.profile(email);

        assertEquals(email, response.getEmail());
        assertEquals("John", response.getFirstName());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testProfile_userNotFound_throwsException() {
        String email = "yilmaz@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.profile(email);
        });

        verify(userRepository).findByEmail(email);
    }


    @Test
    void testClaimTicket_successfulClaim_returnResponse() {
        String email = "furkanyilmaz@email.com";
        Long ticketId = 1L;
        BigDecimal prizeAmount = BigDecimal.valueOf(750);
        BigDecimal initialBalance = BigDecimal.valueOf(1000);

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(ticketId);
        when(ticket.getPrizeAmount()).thenReturn(prizeAmount);
        when(ticket.getTicketNumber()).thenReturn("TKT-6161");
        when(ticket.getClaimedAt()).thenReturn(java.time.LocalDateTime.now());
        when(ticket.isClaimable()).thenReturn(true);

        User user = spy(User.builder()
                .email(email)
                .balance(initialBalance)
                .tickets(List.of(ticket))
                .build());

        doNothing().when(ticket).claim();
        doReturn(ticket).when(user).getTicketById(ticketId);

        when(userRepository.findByEmailWithTickets(email)).thenReturn(Optional.of(user));
        var response = userService.claimTicket(email, ticketId);

        assertEquals(ticketId, response.getTicketId());
        assertEquals(prizeAmount, response.getClaimedAmount());
        assertEquals(initialBalance.add(prizeAmount), response.getNewBalance());

        verify(userRepository).save(user);
        verify(ticket).claim();
    }

    @Test
    void testClaimTicket_ticketNotBelongToUser_throwsException(){
        String email = "furkan@email.com";
        Long ticketId = 10L;

        User user = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.claimTicket(email, ticketId);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void testClaimTicket_ticketNotClaimable_throwsException() {
        String email = "furkan@email.com";
        Long ticketId = 15L;

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(ticketId);
        when(ticket.isClaimable()).thenReturn(false);

        User user = spy(User.builder()
                .email(email)
                .tickets(List.of(ticket))
                .build());

        doReturn(ticket).when(user).getTicketById(ticketId);
        doThrow(new IllegalStateException("Ticket is not claimable")).when(user).claimTicket(ticketId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> {
            userService.claimTicket(email, ticketId);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void testUserAllTickets_returnsPagedTickets() {
        String email = "furkan@email.com";

        Ticket ticket1 = mock(Ticket.class);
        Ticket ticket2 = mock(Ticket.class);
        Ticket ticket3 = mock(Ticket.class);

        when(ticket1.getStatus()).thenReturn(TicketStatus.WAITING_FOR_DRAW);
        when(ticket2.getStatus()).thenReturn(TicketStatus.WON);
        when(ticket3.getStatus()).thenReturn(TicketStatus.NOT_WON);

        when(ticket1.getTicketNumber()).thenReturn("T1");
        when(ticket2.getTicketNumber()).thenReturn("T2");
        when(ticket3.getTicketNumber()).thenReturn("T3");

        when(ticket1.getSelectedNumbers()).thenReturn("1,2,3,4,5");
        when(ticket2.getSelectedNumbers()).thenReturn("6,7,8,9,10");
        when(ticket3.getSelectedNumbers()).thenReturn("11,12,13,14,15");

        when(ticket1.getPurchaseTimestamp()).thenReturn(LocalDateTime.of(2025, 6, 1, 12, 0));
        when(ticket2.getPurchaseTimestamp()).thenReturn(LocalDateTime.of(2025, 6, 2, 12, 0));
        when(ticket3.getPurchaseTimestamp()).thenReturn(LocalDateTime.of(2025, 6, 3, 12, 0));

        Draw draw1 = mock(Draw.class);
        Draw draw2 = mock(Draw.class);
        Draw draw3 = mock(Draw.class);

        when(draw1.getId()).thenReturn(1L);
        when(draw2.getId()).thenReturn(2L);
        when(draw3.getId()).thenReturn(3L);

        when(draw1.getStatus()).thenReturn(DrawStatus.DRAW_OPEN);
        when(draw2.getStatus()).thenReturn(DrawStatus.DRAW_FINALIZED);
        when(draw3.getStatus()).thenReturn(DrawStatus.DRAW_FINALIZED);


        when(draw1.getDrawDate()).thenReturn(LocalDateTime.of(2025, 6, 1, 14, 0));
        when(draw2.getDrawDate()).thenReturn(LocalDateTime.of(2025, 6, 2, 14, 0));
        when(draw3.getDrawDate()).thenReturn(LocalDateTime.of(2025, 6, 3, 14, 0));

        when(ticket1.getDraw()).thenReturn(draw1);
        when(ticket2.getDraw()).thenReturn(draw2);
        when(ticket3.getDraw()).thenReturn(draw3);

        List<Ticket> allTickets = List.of(ticket1, ticket2, ticket3);

        User user = spy(User.builder()
                .email(email)
                .tickets(allTickets)
                .build());

        doReturn(allTickets.stream()
                .sorted((a, b) -> b.getPurchaseTimestamp().compareTo(a.getPurchaseTimestamp()))
                .collect(Collectors.toList())).when(user).getAllTicketsSortedByDate();

        when(userRepository.findByEmailWithTickets(email)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 2); // ilk sayfa, 2 item

        PageResponse<TicketDetailResponse> result = userService.userAllTickets(email, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        verify(userRepository).findByEmailWithTickets(email);
    }

    @Test
    void testWinningTickets_returnsWinningTicketDetails() {
        String email = "furkan@email.com";

        Ticket ticket = mock(Ticket.class);

        when(ticket.getStatus()).thenReturn(TicketStatus.WON);
        when(ticket.getTicketNumber()).thenReturn("TKT-6161");
        when(ticket.getSelectedNumbers()).thenReturn("1,2,3,4,5");
        when(ticket.getPurchaseTimestamp()).thenReturn(LocalDateTime.of(2025, 6, 1, 12, 0));

        Draw draw = mock(Draw.class);
        when(draw.getId()).thenReturn(1L);
        when(draw.getDrawDate()).thenReturn(LocalDateTime.of(2025, 6, 2, 18, 0));
        when(draw.getStatus()).thenReturn(DrawStatus.DRAW_FINALIZED);
        when(ticket.getDraw()).thenReturn(draw);

        List<Ticket> winningTickets = List.of(ticket);

        User user = spy(User.builder()
                .email(email)
                .tickets(winningTickets)
                .build());

        doReturn(winningTickets).when(user).getWinningTickets();
        when(userRepository.findByEmailWithTickets(email)).thenReturn(Optional.of(user));

        List<TicketDetailResponse> result = userService.winningTickets(email);

        assertEquals(1, result.size());
        assertEquals("TKT-6161", result.get(0).getTicketNumber());

        verify(userRepository).findByEmailWithTickets(email);
        verify(user).getWinningTickets();
    }

}
