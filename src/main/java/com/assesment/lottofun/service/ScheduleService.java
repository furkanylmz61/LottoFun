package com.assesment.lottofun.service;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final DrawService drawService;
    private final TaskScheduler taskScheduler;

    @PostConstruct
    public void init() {
        Draw active = findOrCreateActiveDraw();
        scheduleDrawExecution(active);
    }

    private Draw findOrCreateActiveDraw() {
        try {
            return drawService.activeDraw();
        } catch (ResourceNotFoundException ex) {
            return drawService.newDraw();
        } catch (Exception ex) {
            throw new BusinessException("Failed to initialize first draw");
        }
    }

    private void scheduleDrawExecution(Draw draw) {

        LocalDateTime scheduleDrawDate = draw.getDrawDate();
        long delayMs = Duration.between(LocalDateTime.now(), scheduleDrawDate).toMillis();
        if (delayMs < 0) delayMs = 0;

        taskScheduler.schedule(() -> {

            try {
                drawService.process(draw);
            } catch (BusinessException bex) {
                log.warn("Business error during draw execution: {}", bex.getMessage());
            } catch (Exception ex) {
                log.error("Unexpected error during draw execution", ex);
            }

            try {
                Draw newDraw = drawService.newDraw();
                scheduleDrawExecution(newDraw);
            } catch (Exception ex) {
                log.error("Failed to create or schedule next draw", ex);
            }

        }, new Date(System.currentTimeMillis() + delayMs));
    }
}
