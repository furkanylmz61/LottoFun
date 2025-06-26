package com.assesment.lottofun.service;

import com.assesment.lottofun.config.PrizeRulesConfig;
import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.TicketStatus;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.infrastructure.repository.DrawRepository;
import com.assesment.lottofun.infrastructure.repository.TicketRepository;
import com.assesment.lottofun.presentation.dto.common.PageResponse;
import com.assesment.lottofun.presentation.dto.response.DrawResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private PrizeRulesConfig prizeRulesConfig;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private DrawService drawService;

    private Draw sampleDraw;
    private PrizeRulesConfig.Draw drawConfig;

    @BeforeEach
    void setUp() {
        sampleDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().plusHours(1))
                .status(DrawStatus.DRAW_OPEN)
                .totalPrizePool(BigDecimal.valueOf(10_000_000.00))
                .createdAt(LocalDateTime.now())
                .build();

        drawConfig = new PrizeRulesConfig.Draw();
        drawConfig.setFrequencyMinutes(60);
        drawConfig.setProcessingBatchSize(1000);
    }

    @Test
    void getActiveDraw_ShouldReturnActiveDraw_WhenActiveDrawExists() {
        when(drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN))
                .thenReturn(Optional.of(sampleDraw));

        Draw result = drawService.getActiveDraw();

        assertNotNull(result);
        assertEquals(sampleDraw.getId(), result.getId());
        assertEquals(DrawStatus.DRAW_OPEN, result.getStatus());
        verify(drawRepository).findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN);
    }

    @Test
    void getActiveDraw_ShouldThrowResourceNotFoundException_WhenNoActiveDrawExists() {
        when(drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> drawService.getActiveDraw()
        );

        assertEquals("No active draw available", exception.getMessage());
        verify(drawRepository).findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN);
    }





    @Test
    void newDraw_ShouldThrowIllegalStateException_WhenActiveDrawAlreadyExists() {
        when(drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN))
                .thenReturn(Optional.of(sampleDraw));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> drawService.newDraw()
        );

        assertTrue(exception.getMessage().contains("Active draw already exists"));
        verify(drawRepository, never()).save(any(Draw.class));
    }

    @Test
    void process_ShouldProcessDraw_WhenDrawIsEligible() {
        Draw eligibleDraw = Draw.builder()
                .id(1L)
                .status(DrawStatus.DRAW_OPEN)
                .drawDate(LocalDateTime.now().plusHours(1))
                .build();

        when(prizeRulesConfig.getDraw()).thenReturn(drawConfig);
        when(ticketRepository.findByDrawIdAndStatus(eq(1L), eq(TicketStatus.WAITING_FOR_DRAW), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(drawRepository.save(any(Draw.class))).thenAnswer(invocation -> invocation.getArgument(0));


        drawService.process(eligibleDraw);


        assertEquals(DrawStatus.DRAW_FINALIZED, eligibleDraw.getStatus());
        assertNotNull(eligibleDraw.getWinningNumbers());
        assertNotNull(eligibleDraw.getExecutedAt());
        assertNotNull(eligibleDraw.getPrizesDistributedAt());
        verify(drawRepository, times(3)).save(eligibleDraw);
    }



    @Test
    void filter_ShouldReturnPagedDrawResponses() {

        Draw draw1 = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now())
                .status(DrawStatus.DRAW_OPEN)
                .totalPrizePool(BigDecimal.valueOf(10_000_000.00))
                .createdAt(LocalDateTime.now())
                .build();

        Draw draw2 = Draw.builder()
                .id(2L)
                .drawDate(LocalDateTime.now().plusHours(1))
                .status(DrawStatus.DRAW_FINALIZED)
                .totalPrizePool(BigDecimal.valueOf(15_000_000.00))
                .createdAt(LocalDateTime.now())
                .build();

        Page<Draw> drawPage = new PageImpl<>(List.of(draw1, draw2));
        when(drawRepository.findAll(any(Pageable.class))).thenReturn(drawPage);


        PageResponse<DrawResponse> result = drawService.filter(0, 10, "desc");


        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(2L, result.getContent().get(1).getId());
        verify(drawRepository).findAll(any(Pageable.class));
    }

    @Test
    void filter_ShouldHandleAscendingSort() {

        Page<Draw> emptyPage = Page.empty();
        when(drawRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);


        PageResponse<DrawResponse> result = drawService.filter(0, 10, "asc");

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(drawRepository).findAll(any(Pageable.class));
    }
}