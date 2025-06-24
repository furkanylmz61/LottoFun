package com.assesment.lottofun.service;

import com.assesment.lottofun.config.LotteryConfig;
import com.assesment.lottofun.controller.response.DrawResponse;
import com.assesment.lottofun.controller.response.PageResponse;
import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.DrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawRepository drawRepository;
    private final LotteryConfig lotteryConfig;

    @Transactional(readOnly = true)
    public Draw getCurrentActiveDraw() {
        return drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN)
                .filter(draw -> draw.getDrawDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResourceNotFoundException("No valid active draw available"));
    }

    @Transactional
    public Draw createNextDraw() {
        LocalDateTime nextDrawDate = LocalDateTime.now()
                .plusHours(lotteryConfig.getDraw().getFrequencyHours());

        Draw newDraw = Draw.builder()
                .drawDate(nextDrawDate)
                .status(DrawStatus.DRAW_OPEN)
                .build();

        List<Draw> saved = drawRepository.saveAll(List.of(newDraw));
        return saved.get(0);
    }

    @Transactional
    public void registerTicketPurchase(Long drawId, BigDecimal ticketPrice) {
        Draw draw = getDrawById(drawId);

        if (!draw.canAcceptTickets()) {
            throw new IllegalStateException("Draw cannot accept tickets in current status: " + draw.getStatus());
        }

        draw.registerTicket(ticketPrice);
        drawRepository.saveAll(List.of(draw));
    }


    @Transactional(readOnly = true)
    public Draw getDrawById(Long drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Draw not found with ID: " + drawId));
    }

    @Transactional(readOnly = true)
    public PageResponse<DrawResponse> getCompletedDraws(Pageable pageable) {
        List<DrawStatus> completedStatuses = List.of(
                DrawStatus.DRAW_FINALIZED,
                DrawStatus.PAYMENTS_PROCESSING,
                DrawStatus.DRAW_EXTRACTED
        );

        Page<Draw> drawsPage = drawRepository.findByStatusInOrderByDrawDateDesc(completedStatuses, pageable);
        Page<DrawResponse> responsePage = drawsPage.map(DrawResponse::fromEntity);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public List<Draw> getDrawsReadyForExecution() {
        return drawRepository.findByStatusAndDrawDateBefore(DrawStatus.DRAW_OPEN, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean canAcceptTickets(Long drawId) {
        return drawRepository.findById(drawId)
                .map(Draw::canAcceptTickets)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public DrawResponse getCurrentActiveDrawResponse() {
        return DrawResponse.fromEntity(getCurrentActiveDraw());
    }
}
