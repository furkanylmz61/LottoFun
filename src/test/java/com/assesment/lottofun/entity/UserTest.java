package com.assesment.lottofun.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Draw draw1;
    private Draw draw2;
    private List<Ticket> tickets;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("test@email.com")
                .firstName("Furkan")
                .lastName("Yılmaz")
                .balance(BigDecimal.valueOf(1000))
                .build();

        draw1 = Draw.builder()
                .id(1L)
                .status(DrawStatus.DRAW_OPEN)
                .drawDate(LocalDateTime.now().plusHours(1))
                .build();

        draw2 = Draw.builder()
                .id(2L)
                .status(DrawStatus.DRAW_CLOSED)
                .drawDate(LocalDateTime.now().plusHours(2))
                .build();

        tickets = new ArrayList<>();
        
        Ticket winningTicket = Ticket.builder()
                .id(1L)
                .ticketNumber("TKT-001")
                .selectedNumbers("1,2,3,4,5")
                .status(TicketStatus.WON)
                .prizeAmount(BigDecimal.valueOf(500))
                .draw(draw1)
                .user(user)
                .purchaseTimestamp(LocalDateTime.now().minusHours(2))
                .build();

        Ticket losingTicket = Ticket.builder()
                .id(2L)
                .ticketNumber("TKT-002")
                .selectedNumbers("6,7,8,9,10")
                .status(TicketStatus.NOT_WON)
                .prizeAmount(BigDecimal.ZERO)
                .draw(draw1)
                .user(user)
                .purchaseTimestamp(LocalDateTime.now().minusHours(1))
                .build();

        Ticket claimedTicket = Ticket.builder()
                .id(3L)
                .ticketNumber("TKT-003")
                .selectedNumbers("11,12,13,14,15")
                .status(TicketStatus.PRIZE_CLAIMED)
                .prizeAmount(BigDecimal.valueOf(200))
                .draw(draw2)
                .user(user)
                .purchaseTimestamp(LocalDateTime.now().minusHours(3))
                .build();

        tickets.add(winningTicket);
        tickets.add(losingTicket);
        tickets.add(claimedTicket);
        user.setTickets(tickets);
    }

    @Test
    void hasSufficientBalance_ShouldReturnTrue_WhenBalanceIsGreaterThanAmount() {
        BigDecimal amount = BigDecimal.valueOf(500);

        boolean result = user.hasSufficientBalance(amount);

        assertTrue(result);
    }

    @Test
    void hasSufficientBalance_ShouldReturnTrue_WhenBalanceIsEqualToAmount() {
        BigDecimal amount = BigDecimal.valueOf(1000);

        boolean result = user.hasSufficientBalance(amount);

        assertTrue(result);
    }

    @Test
    void hasSufficientBalance_ShouldReturnFalse_WhenBalanceIsLessThanAmount() {
        BigDecimal amount = BigDecimal.valueOf(1500);

        boolean result = user.hasSufficientBalance(amount);

        assertFalse(result);
    }

    @Test
    void deductBalance_ShouldReduceBalance_WhenSufficientBalanceExists() {
        BigDecimal initialBalance = user.getBalance();
        BigDecimal deductionAmount = BigDecimal.valueOf(300);

        user.deductBalance(deductionAmount);

        BigDecimal expectedBalance = initialBalance.subtract(deductionAmount);
        assertEquals(expectedBalance, user.getBalance());
    }

    @Test
    void deductBalance_ShouldThrowException_WhenInsufficientBalance() {
        BigDecimal deductionAmount = BigDecimal.valueOf(1500);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.deductBalance(deductionAmount);
        });
        assertEquals("Insufficient balance", exception.getMessage());
        assertEquals(BigDecimal.valueOf(1000), user.getBalance());
    }

    @Test
    void addBalance_ShouldIncreaseBalance() {
        BigDecimal initialBalance = user.getBalance();
        BigDecimal additionAmount = BigDecimal.valueOf(250);

        user.addBalance(additionAmount);

        BigDecimal expectedBalance = initialBalance.add(additionAmount);
        assertEquals(expectedBalance, user.getBalance());
    }

    @Test
    void hasTicketAlready_ShouldReturnTrue_WhenUserHasTicketWithSameNumbersForDraw() {
        Long drawId = 1L;
        Set<Integer> selectedNumbers = Set.of(1, 2, 3, 4, 5);

        boolean result = user.hasTicketAlready(drawId, selectedNumbers);

        assertTrue(result);
    }

    @Test
    void hasTicketAlready_ShouldReturnFalse_WhenUserDoesNotHaveTicketWithSameNumbers() {
        Long drawId = 1L;
        Set<Integer> selectedNumbers = Set.of(20, 21, 22, 23, 24);

        boolean result = user.hasTicketAlready(drawId, selectedNumbers);

        assertFalse(result);
    }

    @Test
    void hasTicketAlready_ShouldReturnFalse_WhenSameNumbersButDifferentDraw() {
        Long drawId = 3L;
        Set<Integer> selectedNumbers = Set.of(1, 2, 3, 4, 5);

        boolean result = user.hasTicketAlready(drawId, selectedNumbers);

        assertFalse(result);
    }

    @Test
    void getWinningTickets_ShouldReturnOnlyWonTickets() {
        List<Ticket> winningTickets = user.getWinningTickets();

        assertEquals(1, winningTickets.size());
        assertEquals(TicketStatus.WON, winningTickets.get(0).getStatus());
        assertEquals("TKT-001", winningTickets.get(0).getTicketNumber());
    }

    @Test
    void getTicketsForDraw_ShouldReturnTicketsForSpecificDraw() {
        Long drawId = 1L;

        List<Ticket> drawTickets = user.getTicketsForDraw(drawId);

        assertEquals(2, drawTickets.size());
        assertTrue(drawTickets.stream().allMatch(ticket -> ticket.getDraw().getId().equals(drawId)));
    }

    @Test
    void getTicketsForDraw_ShouldReturnEmptyList_WhenNoTicketsForDraw() {
        Long drawId = 99L;

        List<Ticket> drawTickets = user.getTicketsForDraw(drawId);

        assertTrue(drawTickets.isEmpty());
    }

    @Test
    void getClaimableTickets_ShouldReturnOnlyWonTickets() {
        List<Ticket> claimableTickets = user.getClaimableTickets();

        assertEquals(1, claimableTickets.size());
        assertEquals(TicketStatus.WON, claimableTickets.get(0).getStatus());
        assertTrue(claimableTickets.get(0).isClaimable());
    }

    @Test
    void getTicketById_ShouldReturnCorrectTicket_WhenExists() {
        Long ticketId = 1L;

        Ticket ticket = user.getTicketById(ticketId);

        assertNotNull(ticket);
        assertEquals(ticketId, ticket.getId());
        assertEquals("TKT-001", ticket.getTicketNumber());
    }

    @Test
    void getTicketById_ShouldReturnNull_WhenTicketDoesNotExist() {
        Long ticketId = 99L;

        Ticket ticket = user.getTicketById(ticketId);

        assertNull(ticket);
    }

    @Test
    void getAllTicketsSortedByDate_ShouldReturnTicketsInDescendingOrderByPurchaseTime() {
        List<Ticket> sortedTickets = user.getAllTicketsSortedByDate();

        assertEquals(3, sortedTickets.size());
        assertEquals("TKT-002", sortedTickets.get(0).getTicketNumber());
        assertEquals("TKT-001", sortedTickets.get(1).getTicketNumber());
        assertEquals("TKT-003", sortedTickets.get(2).getTicketNumber());
    }

    @Test
    void claimTicket_ShouldSetTicketAsClaimedAndAddBalanceToUser() {
        Ticket winningTicket = user.getTickets().stream()
                .filter(t -> t.getStatus() == TicketStatus.WON)
                .findFirst()
                .orElseThrow();
        
        BigDecimal initialBalance = user.getBalance();
        BigDecimal prizeAmount = winningTicket.getPrizeAmount();

        user.claimTicket(winningTicket);

        assertEquals(TicketStatus.PRIZE_CLAIMED, winningTicket.getStatus());
        assertEquals(initialBalance.add(prizeAmount), user.getBalance());
    }

    @Test
    void builder_ShouldSetDefaultBalance_WhenNotSpecified() {
        // When
        User user = User.builder().build();

        // Then
        assertEquals(BigDecimal.valueOf(1000.00), user.getBalance());
    }

    @Test
    void balanceOperations_ShouldWorkCorrectly_InSequence() {
        user.setBalance(BigDecimal.valueOf(1000));

        user.deductBalance(BigDecimal.valueOf(300));
        assertEquals(BigDecimal.valueOf(700), user.getBalance());

        user.addBalance(BigDecimal.valueOf(150));
        assertEquals(BigDecimal.valueOf(850), user.getBalance());

        user.deductBalance(BigDecimal.valueOf(850));
        assertEquals(BigDecimal.valueOf(0), user.getBalance());

        assertThrows(IllegalArgumentException.class, () -> {
            user.deductBalance(BigDecimal.valueOf(1));
        });
    }

    @Test
    void hasTicketAlready_ShouldHandleDifferentNumberOrdering() {
        Long drawId = 1L;
        Set<Integer> selectedNumbers = Set.of(5, 4, 3, 2, 1);

        boolean result = user.hasTicketAlready(drawId, selectedNumbers);

        assertTrue(result);
    }

    @Test
    void userTicketManagement_ShouldWorkCorrectly_WithEmptyTicketsList() {
        User newUser = User.builder()
                .email("new@email.com")
                .tickets(new ArrayList<>())
                .build();

        assertTrue(newUser.getWinningTickets().isEmpty());
        assertTrue(newUser.getClaimableTickets().isEmpty());
        assertTrue(newUser.getTicketsForDraw(1L).isEmpty());
        assertTrue(newUser.getAllTicketsSortedByDate().isEmpty());
        assertNull(newUser.getTicketById(1L));
        assertFalse(newUser.hasTicketAlready(1L, Set.of(1, 2, 3, 4, 5)));
    }
} 