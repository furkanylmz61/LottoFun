package com.assesment.lottofun.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DrawTest {

    private Draw draw;

    @BeforeEach
    void setup() {
        draw = Draw.builder()
                .id(1L)
                .status(DrawStatus.DRAW_OPEN)
                .drawDate(LocalDateTime.now().plusHours(1))
                .totalPrizePool(BigDecimal.valueOf(10000000.00))
                .build();
    }

    @Test
    void canAcceptTickets_ShouldReturnTrue_WhenDrawIsOpenAndDateIsInFuture() {
        draw.setStatus(DrawStatus.DRAW_OPEN);
        draw.setDrawDate(LocalDateTime.now().plusHours(2));

        boolean result = draw.canAcceptTickets();

        assertTrue(result);
    }

    @Test
    void canAcceptTickets_ShouldReturnFalse_WhenDrawIsClosedEvenIfDateIsInFuture() {
        draw.setStatus(DrawStatus.DRAW_CLOSED);
        draw.setDrawDate(LocalDateTime.now().plusHours(2));

        boolean result = draw.canAcceptTickets();

        assertFalse(result);
    }

    @Test
    void canAcceptTickets_ShouldReturnFalse_WhenDrawIsOpenButDateIsInPast() {
        draw.setStatus(DrawStatus.DRAW_OPEN);
        draw.setDrawDate(LocalDateTime.now().minusHours(1));

        boolean result = draw.canAcceptTickets();

        assertFalse(result);
    }

    @Test
    void canAcceptTickets_ShouldReturnFalse_WhenDrawIsExtracted() {
        draw.setStatus(DrawStatus.DRAW_EXTRACTED);
        draw.setDrawDate(LocalDateTime.now().plusHours(1));

        boolean result = draw.canAcceptTickets();

        assertFalse(result);
    }

    @Test
    void setAsClosed_ShouldChangeStatusToClosed_WhenDrawIsOpen() {
        draw.setStatus(DrawStatus.DRAW_OPEN);

        draw.setAsClosed();

        assertEquals(DrawStatus.DRAW_CLOSED, draw.getStatus());
    }

    @Test
    void setAsClosed_ShouldThrowException_WhenDrawIsNotOpen() {
        draw.setStatus(DrawStatus.DRAW_CLOSED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            draw.setAsClosed();
        });

        assertTrue(exception.getMessage().contains("Draw can only be closed from DRAW_OPEN status"));
        assertTrue(exception.getMessage().contains("DRAW_CLOSED"));
    }

    @Test
    void setAsExtracted_ShouldChangeStatusAndSetWinningNumbers_WhenDrawIsClosed() {
        draw.setStatus(DrawStatus.DRAW_CLOSED);
        LocalDateTime beforeExtraction = LocalDateTime.now();

        draw.setAsExtracted();

        assertEquals(DrawStatus.DRAW_EXTRACTED, draw.getStatus());
        assertNotNull(draw.getWinningNumbers());
        assertNotNull(draw.getExecutedAt());
        assertTrue(draw.getExecutedAt().isAfter(beforeExtraction) || draw.getExecutedAt().isEqual(beforeExtraction));
    }

    @Test
    void setAsExtracted_ShouldThrowException_WhenDrawIsNotClosed() {
        draw.setStatus(DrawStatus.DRAW_OPEN);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            draw.setAsExtracted();
        });

        assertTrue(exception.getMessage().contains("Draw can only be extracted from DRAW_CLOSED status"));
        assertTrue(exception.getMessage().contains("DRAW_OPEN"));
    }

    @Test
    void setAsFinalized_ShouldChangeStatusAndSetPrizesDistributedAt_WhenDrawIsExtracted() {
        draw.setStatus(DrawStatus.DRAW_EXTRACTED);
        LocalDateTime beforeFinalization = LocalDateTime.now();

        draw.setAsFinalized();

        assertEquals(DrawStatus.DRAW_FINALIZED, draw.getStatus());
        assertNotNull(draw.getPrizesDistributedAt());
        assertTrue(draw.getPrizesDistributedAt().isAfter(beforeFinalization) || 
                  draw.getPrizesDistributedAt().isEqual(beforeFinalization));
    }

    @Test
    void setAsFinalized_ShouldThrowException_WhenDrawIsNotExtracted() {
        draw.setStatus(DrawStatus.DRAW_CLOSED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            draw.setAsFinalized();
        });

        assertTrue(exception.getMessage().contains("Draw can only be finalized from DRAW_EXTRACTED status"));
        assertTrue(exception.getMessage().contains("DRAW_CLOSED"));
    }

    @Test
    void createNew_ShouldCreateDrawWithCorrectDefaultValues() {
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);

        Draw newDraw = Draw.createNew(scheduledDate);

        assertNotNull(newDraw);
        assertEquals(scheduledDate, newDraw.getDrawDate());
        assertEquals(DrawStatus.DRAW_OPEN, newDraw.getStatus());
        assertEquals(BigDecimal.valueOf(10_000_000.00), newDraw.getTotalPrizePool());
        assertNull(newDraw.getId());
        assertNull(newDraw.getWinningNumbers());
        assertNull(newDraw.getExecutedAt());
        assertNull(newDraw.getPrizesDistributedAt());
    }

    @Test
    void isEligibleForProcess_ShouldReturnTrue_WhenDrawIsOpen() {
        draw.setStatus(DrawStatus.DRAW_OPEN);

        boolean result = draw.isEligibleForProcess();

        assertTrue(result);
    }

    @Test
    void isEligibleForProcess_ShouldReturnFalse_WhenDrawIsClosed() {
        draw.setStatus(DrawStatus.DRAW_CLOSED);

        boolean result = draw.isEligibleForProcess();

        assertFalse(result);
    }

    @Test
    void isEligibleForProcess_ShouldReturnFalse_WhenDrawIsExtracted() {
        draw.setStatus(DrawStatus.DRAW_EXTRACTED);

        boolean result = draw.isEligibleForProcess();

        assertFalse(result);
    }

    @Test
    void isEligibleForProcess_ShouldReturnFalse_WhenDrawIsFinalized() {
        draw.setStatus(DrawStatus.DRAW_FINALIZED);

        boolean result = draw.isEligibleForProcess();

        assertFalse(result);
    }

    @Test
    void statusTransition_ShouldWorkCorrectly_ThroughFullLifecycle() {
        draw.setStatus(DrawStatus.DRAW_OPEN);
        assertTrue(draw.isEligibleForProcess());

        draw.setAsClosed();
        assertEquals(DrawStatus.DRAW_CLOSED, draw.getStatus());
        assertFalse(draw.isEligibleForProcess());

        draw.setAsExtracted();
        assertEquals(DrawStatus.DRAW_EXTRACTED, draw.getStatus());
        assertNotNull(draw.getWinningNumbers());
        assertNotNull(draw.getExecutedAt());

        draw.setAsFinalized();
        assertEquals(DrawStatus.DRAW_FINALIZED, draw.getStatus());
        assertNotNull(draw.getPrizesDistributedAt());
    }

    @Test
    void builder_ShouldSetDefaultValues_WhenNotSpecified() {
        Draw draw = Draw.builder().build();

        assertEquals(DrawStatus.DRAW_OPEN, draw.getStatus());
        assertEquals(BigDecimal.ZERO, draw.getTotalPrizePool());
    }
} 