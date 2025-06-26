package com.assesment.lottofun.entity;

import com.assesment.lottofun.config.PrizeRulesConfig;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.infrastructure.configuration.PrizeRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private Ticket ticket;
    private User user;
    private Draw draw;

    @BeforeEach
    void setup() {
        PrizeRulesConfig config = new PrizeRulesConfig();
        new PrizeRules(config);
        
        user = User.builder()
                .id(1L)
                .email("furkan@email.com")
                .firstName("Furkan")
                .lastName("Yılmaz")
                .balance(BigDecimal.valueOf(1000))
                .build();

        draw = Draw.builder()
                .id(1L)
                .status(DrawStatus.DRAW_OPEN)
                .drawDate(LocalDateTime.now().plusHours(1))
                .build();

        ticket = Ticket.builder()
                .id(1L)
                .ticketNumber("TKT-12345")
                .selectedNumbers("1,2,3,4,5")
                .purchasePrice(BigDecimal.valueOf(100))
                .status(TicketStatus.WAITING_FOR_DRAW)
                .user(user)
                .draw(draw)
                .build();
    }

    @Test
    void setAsClaimed_ShouldChangeStatusToClaimed_WhenTicketIsClaimable() {
        ticket.setStatus(TicketStatus.WON);
        ticket.setPrizeAmount(BigDecimal.valueOf(500));

        ticket.setAsClaimed();

        assertEquals(TicketStatus.PRIZE_CLAIMED, ticket.getStatus());
    }

    @Test
    void setAsClaimed_ShouldThrowBusinessException_WhenTicketIsNotClaimable() {
        ticket.setStatus(TicketStatus.NOT_WON);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ticket.setAsClaimed();
        });

        assertEquals("Only winning tickets can be claimed", exception.getMessage());
        assertEquals(TicketStatus.NOT_WON, ticket.getStatus());
    }

    @Test
    void isClaimable_ShouldReturnTrue_WhenTicketStatusIsWon() {
        ticket.setStatus(TicketStatus.WON);

        boolean result = ticket.isClaimable();

        assertTrue(result);
    }

    @Test
    void isClaimable_ShouldReturnFalse_WhenTicketStatusIsNotWon() {
        ticket.setStatus(TicketStatus.NOT_WON);

        boolean result = ticket.isClaimable();

        assertFalse(result);
    }

    @Test
    void isClaimable_ShouldReturnFalse_WhenTicketStatusIsWaitingForDraw() {
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);

        boolean result = ticket.isClaimable();

        assertFalse(result);
    }

    @Test
    void isClaimable_ShouldReturnFalse_WhenTicketStatusIsPrizeClaimed() {
        ticket.setStatus(TicketStatus.PRIZE_CLAIMED);

        boolean result = ticket.isClaimable();

        assertFalse(result);
    }

    @Test
    void calculateResult_ShouldSetStatusToWon_WhenMatchCountIsAtLeastTwo() {
        ticket.setSelectedNumbers("1,2,3,4,5");
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);
        String winningNumbers = "1,2,6,7,8"; // 2 matches

        ticket.calculateResult(winningNumbers);

        assertEquals(TicketStatus.WON, ticket.getStatus());
        assertEquals(Integer.valueOf(2), ticket.getMatchCount());
        assertNotNull(ticket.getPrizeAmount());
        assertTrue(ticket.getPrizeAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateResult_ShouldSetStatusToNotWon_WhenMatchCountIsLessThanTwo() {
        ticket.setSelectedNumbers("1,2,3,4,5");
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);
        String winningNumbers = "1,6,7,8,9"; // 1 match

        ticket.calculateResult(winningNumbers);

        assertEquals(TicketStatus.NOT_WON, ticket.getStatus());
        assertEquals(Integer.valueOf(1), ticket.getMatchCount());
        assertNotNull(ticket.getPrizeAmount());
        assertEquals(BigDecimal.ZERO, ticket.getPrizeAmount());
    }

    @Test
    void calculateResult_ShouldSetStatusToNotWon_WhenNoMatches() {
        ticket.setSelectedNumbers("1,2,3,4,5");
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);
        String winningNumbers = "6,7,8,9,10"; // 0 matches

        ticket.calculateResult(winningNumbers);

        assertEquals(TicketStatus.NOT_WON, ticket.getStatus());
        assertEquals(Integer.valueOf(0), ticket.getMatchCount());
        assertNotNull(ticket.getPrizeAmount());
        assertEquals(BigDecimal.ZERO, ticket.getPrizeAmount());
    }

    @Test
    void calculateResult_ShouldSetStatusToWon_WhenAllNumbersMatch() {
        ticket.setSelectedNumbers("1,2,3,4,5");
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);
        String winningNumbers = "1,2,3,4,5"; // 5 matches

        ticket.calculateResult(winningNumbers);

        assertEquals(TicketStatus.WON, ticket.getStatus());
        assertEquals(Integer.valueOf(5), ticket.getMatchCount());
        assertNotNull(ticket.getPrizeAmount());
        assertTrue(ticket.getPrizeAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateResult_ShouldThrowException_WhenTicketStatusIsNotWaitingForDraw() {
        ticket.setStatus(TicketStatus.WON);
        String winningNumbers = "1,2,3,4,5";

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ticket.calculateResult(winningNumbers);
        });

        assertTrue(exception.getMessage().contains("Ticket can only be marked as extracted from WAITING_FOR_DRAW status"));
        assertTrue(exception.getMessage().contains("WON"));
    }

    @Test
    void createNew_ShouldCreateTicketWithCorrectValues() {
        Set<Integer> selectedNumbers = Set.of(1, 2, 3, 4, 5);
        BigDecimal purchasePrice = BigDecimal.valueOf(150);

        Ticket newTicket = Ticket.createNew(user, draw, selectedNumbers, purchasePrice);

        assertNotNull(newTicket);
        assertEquals("1,2,3,4,5", newTicket.getSelectedNumbers());
        assertEquals(purchasePrice, newTicket.getPurchasePrice());
        assertEquals(TicketStatus.WAITING_FOR_DRAW, newTicket.getStatus());
        assertEquals(draw, newTicket.getDraw());
        assertEquals(user, newTicket.getUser());
        assertNull(newTicket.getId());
        assertNull(newTicket.getTicketNumber());
    }

    @Test
    void calculateResult_ShouldHandleDifferentOrderNumbers_Correctly() {

        ticket.setSelectedNumbers("5,3,1,4,2");
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);
        String winningNumbers = "1,2,6,7,8";


        ticket.calculateResult(winningNumbers);


        assertEquals(TicketStatus.WON, ticket.getStatus());
        assertEquals(Integer.valueOf(2), ticket.getMatchCount());
    }

    @Test
    void calculateResult_ShouldHandleDuplicateNumbers_Correctly() {
        ticket.setSelectedNumbers("1,2,3,4,5");
        ticket.setStatus(TicketStatus.WAITING_FOR_DRAW);
        String winningNumbers = "1,1,2,2,3";


        ticket.calculateResult(winningNumbers);


        assertEquals(TicketStatus.WON, ticket.getStatus());
        assertEquals(Integer.valueOf(3), ticket.getMatchCount());
    }

    @Test
    void ticket_ShouldHaveCorrectDefaultStatus() {

        Ticket ticket = Ticket.builder().build();


        assertEquals(TicketStatus.WAITING_FOR_DRAW, ticket.getStatus());
    }

    @Test
    void ticketLifecycle_ShouldWorkCorrectly_FromCreationToClaim() {
        Set<Integer> numbers = Set.of(1, 2, 3, 4, 5);
        Ticket newTicket = Ticket.createNew(user, draw, numbers, BigDecimal.valueOf(100));
        assertEquals(TicketStatus.WAITING_FOR_DRAW, newTicket.getStatus());


        newTicket.calculateResult("1,2,3,6,7");
        assertEquals(TicketStatus.WON, newTicket.getStatus());
        assertEquals(Integer.valueOf(3), newTicket.getMatchCount());
        assertTrue(newTicket.isClaimable());

        newTicket.setAsClaimed();
        assertEquals(TicketStatus.PRIZE_CLAIMED, newTicket.getStatus());
        assertFalse(newTicket.isClaimable());
    }

    @Test
    void ticketLifecycle_ShouldWorkCorrectly_ForLosingTicket() {
        Set<Integer> numbers = Set.of(1, 2, 3, 4, 5);
        Ticket newTicket = Ticket.createNew(user, draw, numbers, BigDecimal.valueOf(100));

        newTicket.calculateResult("6,7,8,9,10");
        assertEquals(TicketStatus.NOT_WON, newTicket.getStatus());
        assertEquals(Integer.valueOf(0), newTicket.getMatchCount());
        assertFalse(newTicket.isClaimable());

        assertThrows(BusinessException.class, () -> {
            newTicket.setAsClaimed();
        });
    }
} 