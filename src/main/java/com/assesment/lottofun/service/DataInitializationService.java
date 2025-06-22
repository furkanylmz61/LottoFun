package com.assesment.lottofun.service;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import com.assesment.lottofun.repository.DrawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializationService implements ApplicationRunner {

    private final DrawRepository drawRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initializeFirstDraw();
    }

    /**
     * Creates the first draw if no draws exist in the database
     */
    private void initializeFirstDraw() {
        log.info("Checking if initial draw needs to be created");

        if (drawRepository.count() == 0) {
            log.info("No draws found, creating initial draw");

            Draw initialDraw = Draw.builder()
                    .drawDate(LocalDateTime.now().plusHours(24))
                    .status(DrawStatus.DRAW_OPEN)
                    .totalTickets(0)
                    .totalPrizePool(BigDecimal.ZERO)
                    .build();

            Draw savedDraw = drawRepository.save(initialDraw);
            log.info("Initial draw created with ID: {} scheduled for: {}",
                    savedDraw.getId(), savedDraw.getDrawDate());
        } else {
            log.info("Draws already exist, skipping initialization");
        }
    }
}