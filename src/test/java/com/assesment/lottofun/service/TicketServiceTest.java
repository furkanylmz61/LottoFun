package com.assesment.lottofun.service;

import com.assesment.lottofun.config.LotteryConfig;
import com.assesment.lottofun.controller.request.TicketPurchaseRequest;
import com.assesment.lottofun.controller.response.TicketBasicResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.entity.*;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private DrawService drawService;
    @Mock private UserService userService;
    @Mock private LotteryConfig lotteryConfig;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPurchase_successfulTicketPurchase_returnsBasicResponse() {
        String email = "test@email.com";
        Set<Integer> numbers = Set.of(1, 2, 3, 4, 5);
        BigDecimal price = BigDecimal.valueOf(100);

        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setSelectedNumbers(numbers);

        LotteryConfig.Ticket ticketCfg = mock(LotteryConfig.Ticket.class);
        when(lotteryConfig.getTicket()).thenReturn(ticketCfg);
        when(ticketCfg.getPrice()).thenReturn(price);

        User user = mock(User.class);
        Draw draw = mock(Draw.class);
        when(drawService.activeDraw()).thenReturn(draw);
        when(draw.canAcceptTickets()).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(user);

        doNothing().when(user).deductBalance(price);
        doNothing().when(userService).save(user);

        Ticket ticket = Ticket.createNew(user, draw, numbers, price);
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        doNothing().when(draw).registerTicket(price);
        doNothing().when(drawService).save(draw);

        TicketBasicResponse expectedResponse = TicketBasicResponse.fromEntity(ticket);

        TicketBasicResponse result = ticketService.purchase(email, request);

        assertNotNull(result);
        assertEquals(expectedResponse.getTicketNumber(), result.getTicketNumber());
        verify(ticketRepository).save(any(Ticket.class));
        verify(user).deductBalance(price);
    }

    @Test
    void testPurchase_drawClosed_throwsBusinessException() {
        when(drawService.activeDraw()).thenReturn(mock(Draw.class));
        when(drawService.activeDraw().canAcceptTickets()).thenReturn(false);

        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setSelectedNumbers(Set.of(1,2,3,4,5));

        assertThrows(BusinessException.class, () -> {
            ticketService.purchase("furkan@email.com", request);
        });
    }

    @Test
    void testTicketDetail_successful_returnsTicketDetailResponse() {
        String email = "furkan@email.com";
        Long ticketId = 1L;

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(ticketId);
        when(ticket.getStatus()).thenReturn(TicketStatus.WAITING_FOR_DRAW);
        when(ticket.getTicketNumber()).thenReturn("TKT-6161");
        when(ticket.getSelectedNumbers()).thenReturn("1,2,3,4,5");
        when(ticket.getPurchaseTimestamp()).thenReturn(LocalDateTime.now());

        Draw draw = mock(Draw.class);
        when(draw.getStatus()).thenReturn(DrawStatus.DRAW_OPEN);
        when(draw.getDrawDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(draw.getId()).thenReturn(42L);
        when(ticket.getDraw()).thenReturn(draw);

        User user = mock(User.class);
        when(user.getTickets()).thenReturn(List.of(ticket));
        when(userService.getUserByEmail(email)).thenReturn(user);


        TicketDetailResponse response = ticketService.ticketDetail(email, ticketId);


        assertNotNull(response);
        assertEquals("TKT-6161", response.getTicketNumber());
        verify(userService).getUserByEmail(email);
    }

}
