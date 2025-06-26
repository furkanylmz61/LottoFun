package com.assesment.lottofun.service;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private DrawService drawService;

    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private ScheduleService scheduleService;

    private Draw sampleDraw;

    @BeforeEach
    void setup() {
        sampleDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().plusHours(1))
                .status(DrawStatus.DRAW_OPEN)
                .build();
    }

    @Test
    void init_ShouldUseExistingActiveDraw_WhenActiveDrawExists() {
        when(drawService.getActiveDraw()).thenReturn(sampleDraw);

        scheduleService.init();

        verify(drawService).getActiveDraw();
        verify(drawService, never()).newDraw();
        verify(taskScheduler).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void init_ShouldCreateNewDraw_WhenNoActiveDrawExists() {
        when(drawService.getActiveDraw()).thenThrow(new ResourceNotFoundException("No active draw"));
        when(drawService.newDraw()).thenReturn(sampleDraw);

        scheduleService.init();

        verify(drawService).getActiveDraw();
        verify(drawService).newDraw();
        verify(taskScheduler).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void init_ShouldThrowBusinessException_WhenGetActiveDrawFailsWithNonResourceNotFound() {
        when(drawService.getActiveDraw()).thenThrow(new RuntimeException("Database error"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scheduleService.init();
        });

        assertEquals("Failed to initialize first draw", exception.getMessage());
        verify(drawService).getActiveDraw();
        verify(drawService, never()).newDraw();
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void init_ShouldHandleResourceNotFoundAndCreateNewDraw() {
        when(drawService.getActiveDraw()).thenThrow(new ResourceNotFoundException("No active draw found"));
        when(drawService.newDraw()).thenReturn(sampleDraw);

        assertDoesNotThrow(() -> scheduleService.init());

        verify(drawService).getActiveDraw();
        verify(drawService).newDraw();
        verify(taskScheduler).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void init_ShouldScheduleTask_WhenValidDrawExists() {
        when(drawService.getActiveDraw()).thenReturn(sampleDraw);

        scheduleService.init();

        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void init_ShouldCallDrawService_ToGetActiveDraw() {
        when(drawService.getActiveDraw()).thenReturn(sampleDraw);

        scheduleService.init();

        verify(drawService, times(1)).getActiveDraw();
    }

    @Test
    void init_ShouldHandlePastDrawDate_Gracefully() {
        Draw pastDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().minusHours(1))
                .status(DrawStatus.DRAW_OPEN)
                .build();
        
        when(drawService.getActiveDraw()).thenReturn(pastDraw);

        assertDoesNotThrow(() -> scheduleService.init());

        verify(drawService).getActiveDraw();
        verify(taskScheduler).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void init_ShouldHandleFutureDrawDate_Gracefully() {
        Draw futureDraw = Draw.builder()
                .id(1L)
                .drawDate(LocalDateTime.now().plusDays(1))
                .status(DrawStatus.DRAW_OPEN)
                .build();
        
        when(drawService.getActiveDraw()).thenReturn(futureDraw);

        assertDoesNotThrow(() -> scheduleService.init());

        verify(drawService).getActiveDraw();
        verify(taskScheduler).schedule(any(Runnable.class), any(Date.class));
    }
} 