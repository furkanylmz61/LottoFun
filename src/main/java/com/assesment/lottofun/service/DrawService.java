package com.assesment.lottofun.service;

import com.assesment.lottofun.config.LotteryConfig;
import com.assesment.lottofun.dto.response.DrawResponse;
import com.assesment.lottofun.dto.response.PageResponse;
import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.DrawRepository;
import com.assesment.lottofun.util.LotteryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawRepository drawRepository;
    private final LotteryConfig lotteryConfig;

    /**
     * Gets the current active draw (DRAW_OPEN status)
     */
    @Transactional(readOnly = true)
    public Draw getCurrentActiveDraw() {
        log.debug("Getting current active draw");

        Optional<Draw> activeDraw = drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN);

        if (activeDraw.isEmpty()) {
            log.info("No active draw found, creating new one");
            return createNextDraw();
        }

        Draw draw = activeDraw.get();

        // Check if draw date has passed
        if (draw.getDrawDate().isBefore(LocalDateTime.now())) {
            log.info("Active draw {} has expired, creating new one", draw.getId());
            return createNextDraw();
        }

        return draw;
    }

    /**
     * Creates the next draw with scheduled date
     */
    @Transactional
    public Draw createNextDraw() {
        log.info("Creating next draw");

        // Calculate next draw date (current time + frequency hours)
        LocalDateTime nextDrawDate = LocalDateTime.now()
                .plusHours(lotteryConfig.getDraw().getFrequencyHours());

        // Get the next draw ID
        Long nextDrawId = getNextDrawId();

        Draw newDraw = Draw.builder()
                .drawDate(nextDrawDate)
                .status(DrawStatus.DRAW_OPEN)
                .totalTickets(0)
                .totalPrizePool(BigDecimal.ZERO)
                .build();

        Draw savedDraw = drawRepository.save(newDraw);
        log.info("Created new draw with ID: {} scheduled for: {}", savedDraw.getId(), savedDraw.getDrawDate());

        return savedDraw;
    }

    /**
     * Gets next available draw ID
     */
    private Long getNextDrawId() {
        Optional<Draw> lastDraw = drawRepository.findTopByOrderByIdDesc();
        return lastDraw.map(draw -> draw.getId() + 1).orElse(1L);
    }

    /**
     * Registers a ticket purchase for a draw
     */
    @Transactional
    public void registerTicketPurchase(Long drawId, BigDecimal ticketPrice) {
        log.debug("Registering ticket purchase for draw: {} with price: {}", drawId, ticketPrice);

        Draw draw = drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Draw not found with ID: " + drawId));

        if (!draw.canAcceptTickets()) {
            throw new IllegalStateException("Draw cannot accept tickets in current status: " + draw.getStatus());
        }

        draw.registerTicket(ticketPrice);
        drawRepository.save(draw);

        log.debug("Updated draw {} - Total tickets: {}, Total prize pool: {}",
                drawId, draw.getTotalTickets(), draw.getTotalPrizePool());
    }

    /**
     * Gets draw by ID
     */
    @Transactional(readOnly = true)
    public Draw getDrawById(Long drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Draw not found with ID: " + drawId));
    }

    /**
     * Gets completed draws with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<DrawResponse> getCompletedDraws(Pageable pageable) {
        log.debug("Getting completed draws with pagination: {}", pageable);

        List<DrawStatus> completedStatuses = List.of(
                DrawStatus.DRAW_FINALIZED,
                DrawStatus.PAYMENTS_PROCESSING,
                DrawStatus.DRAW_EXTRACTED
        );

        Page<Draw> drawsPage = drawRepository.findByStatusInOrderByDrawDateDesc(completedStatuses, pageable);

        Page<DrawResponse> responsePage = drawsPage.map(this::convertToDrawResponse);

        return PageResponse.from(responsePage);
    }

    /**
     * Converts Draw entity to DrawResponse DTO
     */
    private DrawResponse convertToDrawResponse(Draw draw) {
        DrawResponse response = new DrawResponse();
        response.setId(draw.getId());
        response.setDrawDate(draw.getDrawDate());
        response.setStatus(draw.getStatus().name());
        response.setTotalTickets(draw.getTotalTickets());
        response.setTotalPrizePool(draw.getTotalPrizePool());
        response.setExecutedAt(draw.getExecutedAt());
        response.setPrizesDistributedAt(draw.getPrizesDistributedAt());
        response.setCreatedAt(draw.getCreatedAt());

        // Convert winning numbers string to list
        if (draw.getWinningNumbers() != null && !draw.getWinningNumbers().trim().isEmpty()) {
            response.setWinningNumbers(LotteryUtils.stringToNumbers(draw.getWinningNumbers()));
        }

        return response;
    }

    /**
     * Gets all draws ready for execution (past draw date and DRAW_OPEN status)
     */
    @Transactional(readOnly = true)
    public List<Draw> getDrawsReadyForExecution() {
        return drawRepository.findByStatusAndDrawDateBefore(DrawStatus.DRAW_OPEN, LocalDateTime.now());
    }

    /**
     * Checks if a draw exists and can accept tickets
     */
    @Transactional(readOnly = true)
    public boolean canAcceptTickets(Long drawId) {
        Optional<Draw> draw = drawRepository.findById(drawId);
        return draw.map(Draw::canAcceptTickets).orElse(false);
    }

    /**
     * Gets the current active draw response for API
     */
    @Transactional(readOnly = true)
    public DrawResponse getCurrentActiveDrawResponse() {
        Draw activeDraw = getCurrentActiveDraw();
        return convertToDrawResponse(activeDraw);
    }
}