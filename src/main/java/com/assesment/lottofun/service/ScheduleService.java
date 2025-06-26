package com.assesment.lottofun.service;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final DrawService drawService;
    private final TaskScheduler taskScheduler;

    @PostConstruct
    public void init() {
        Draw activeDraw = findOrCreateActiveDraw();
        scheduleDrawExecution(activeDraw);
    }

    private Draw findOrCreateActiveDraw() {
        try {
            return drawService.getActiveDraw();
        } catch (ResourceNotFoundException ex) {
            return drawService.newDraw();
        } catch (Exception ex) {
            throw new BusinessException("Failed to initialize first draw");
        }
    }

    private void scheduleDrawExecution(Draw draw) {
        LocalDateTime drawDate = draw.getDrawDate();
        long delayMs = Duration.between(LocalDateTime.now(), drawDate).toMillis();

        if (delayMs < 0) {
            delayMs = 0;
        }


        taskScheduler.schedule(() -> executeDrawAndScheduleNew(),
                new Date(System.currentTimeMillis() + delayMs));
    }

    @Transactional
    void executeDrawAndScheduleNew() {
        Draw lockDraw = drawService.getLockDraw();
        drawService.process(lockDraw);
        Draw nextDraw = drawService.newDraw();
        scheduleDrawExecution(nextDraw);
    }
}