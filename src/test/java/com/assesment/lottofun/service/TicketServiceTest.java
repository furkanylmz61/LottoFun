package com.assesment.lottofun.service;

import com.assesment.lottofun.config.PrizeRulesConfig;
import com.assesment.lottofun.presentation.dto.request.TicketPurchaseRequest;
import com.assesment.lottofun.presentation.dto.response.TicketBasicResponse;
import com.assesment.lottofun.presentation.dto.response.TicketDetailResponse;
import com.assesment.lottofun.entity.*;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.infrastructure.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private DrawService drawService;
    @Mock private UserService userService;
    @Mock private PrizeRulesConfig prizeRulesConfig;

    @InjectMocks
    private TicketService ticketService;

    private User sampleUser;
    private Draw sampleDraw;
    private PrizeRulesConfig.Ticket ticketConfig;

    @BeforeEach
    void setup() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .firstName("Test")
                .lastName("User")
                .balance(BigDecimal.valueOf(1000))
                .build();

        sampleDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().plusHours(1))
                .status(DrawStatus.DRAW_OPEN)
                .build();

        ticketConfig = new PrizeRulesConfig.Ticket();
        ticketConfig.setPrice(BigDecimal.valueOf(10.00));
    }

    @Test
    void purchase_ShouldReturnTicketBasicResponse_WhenSuccessful() {
        String email = "test@email.com";
        Set<Integer> numbers = Set.of(1, 2, 3, 4, 5);
        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setSelectedNumbers(numbers);

        when(drawService.getActiveDraw()).thenReturn(sampleDraw);
        when(userService.getUserByEmail(email)).thenReturn(sampleUser);
        when(prizeRulesConfig.getTicket()).thenReturn(ticketConfig);

        Ticket savedTicket = Ticket.createNew(sampleUser, sampleDraw, numbers, ticketConfig.getPrice());
        savedTicket.setId(1L);
        savedTicket.setTicketNumber("TKT-12345-TEST");
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketBasicResponse result = ticketService.purchase(email, request);

        assertNotNull(result);
        assertNotNull(result.getTicketNumber());
        verify(drawService).getActiveDraw();
        verify(userService).getUserByEmail(email);
        verify(userService).save(sampleUser);
        verify(ticketRepository).save(any(Ticket.class));
        verify(drawService).save(sampleDraw);
    }

    @Test
    void purchase_ShouldThrowBusinessException_WhenDrawCannotAcceptTickets() {
        Draw closedDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().minusHours(1))
                .status(DrawStatus.DRAW_CLOSED)
                .build();

        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setSelectedNumbers(Set.of(1, 2, 3, 4, 5));

        when(drawService.getActiveDraw()).thenReturn(closedDraw);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ticketService.purchase("test@email.com", request);
        });

        assertEquals("The current active draw is no longer accepting tickets", exception.getMessage());
        verify(drawService).getActiveDraw();
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void ticketDetail_ShouldReturnTicketDetailResponse_WhenTicketExists() {
        String email = "test@email.com";
        Long ticketId = 1L;

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .ticketNumber("TKT-12345")
                .selectedNumbers("1,2,3,4,5")
                .status(TicketStatus.WAITING_FOR_DRAW)
                .purchaseTimestamp(LocalDateTime.now())
                .draw(sampleDraw)
                .user(sampleUser)
                .build();

        sampleUser.setTickets(List.of(ticket));
        when(userService.getUserByEmail(email)).thenReturn(sampleUser);

        TicketDetailResponse response = ticketService.ticketDetail(email, ticketId);

        assertNotNull(response);
        assertEquals("TKT-12345", response.getTicketNumber());
        assertEquals(TicketStatus.WAITING_FOR_DRAW.name(), response.getTicketStatus());
        verify(userService).getUserByEmail(email);
    }

    @Test
    void ticketDetail_ShouldThrowResourceNotFoundException_WhenTicketNotFound() {
        String email = "test@email.com";
        Long ticketId = 999L;

        sampleUser.setTickets(List.of());
        when(userService.getUserByEmail(email)).thenReturn(sampleUser);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.ticketDetail(email, ticketId);
        });

        assertTrue(exception.getMessage().contains("Ticket not found"));
        verify(userService).getUserByEmail(email);
    }

}
