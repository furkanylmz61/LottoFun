package com.assesment.lottofun.service;

import com.assesment.lottofun.config.LotteryConfig;
import com.assesment.lottofun.controller.response.DrawResponse;
import com.assesment.lottofun.controller.response.PageResponse;
import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.DrawStatus;
import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.TicketStatus;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.DrawRepository;
import com.assesment.lottofun.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawRepository drawRepository;
    private final LotteryConfig lotteryConfig;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public Draw activeDraw() {
        return drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN)
                .filter(draw -> draw.getDrawDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResourceNotFoundException("No active draw available"));
    }

    @Transactional
    public Draw newDraw() {
        drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN)
                .ifPresent(activeDraw -> {
                    throw new IllegalStateException("Active draw already exists: " + activeDraw.getId());
                });

        LocalDateTime scheduledDate = LocalDateTime.now()
                .plusMinutes(lotteryConfig.getDraw().getFrequencyMinutes());

        Draw newDraw = Draw.builder()
                .drawDate(scheduledDate)
                .status(DrawStatus.DRAW_OPEN)
                .build();

        return drawRepository.save(newDraw);
    }

    @Transactional
    public void process(Draw draw) {
        if (!draw.isEligibleForProcess()) {
            return;
        }

        draw.setAsClosed();
        drawRepository.save(draw);
        draw.setAsExtracted();
        drawRepository.save(draw);

        processTickets(draw);

        draw.setAsFinalized(createPrizePercentageMap());
        draw.markPrizesDistributed();
        drawRepository.save(draw);
    }

    private void processTickets(Draw draw) {
        Map<Integer, Long> matchCountTotals = calculateMatchCounts(draw);
        assignPrizes(draw, matchCountTotals);
    }

    private Map<Integer, Long> calculateMatchCounts(Draw draw) {
        Map<Integer, Long> matchCountTotals = new HashMap<>();
        int page = 0;
        int size = 1000;

        while (true) {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<Ticket> ticketPage = ticketRepository.findByDrawIdAndStatus(
                    draw.getId(), TicketStatus.WAITING_FOR_DRAW, pageable);

            List<Ticket> tickets = ticketPage.getContent();
            if (tickets.isEmpty()) {
                break;
            }

            for (Ticket ticket : tickets) {
                ticket.markAsExtracted(draw.getWinningNumbers());
                int matchCount = ticket.getMatchCount();
                matchCountTotals.put(matchCount, matchCountTotals.getOrDefault(matchCount, 0L) + 1);
            }

            ticketRepository.saveAll(tickets);

            if (!ticketPage.hasNext()) {
                break;
            }
            page++;
        }

        return matchCountTotals;
    }

    private void assignPrizes(Draw draw, Map<Integer, Long> matchCountTotals) {
        BigDecimal totalPrizePool = draw.getTotalPrizePool();
        Map<Integer, Integer> prizePercentages = createPrizePercentageMap();
        Map<Integer, BigDecimal> prizePerTicket = calculatePrizePerTicket(totalPrizePool, matchCountTotals, prizePercentages);

        int page = 0;
        int size = 1000;

        while (true) {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<Ticket> ticketPage = ticketRepository.findByDrawIdAndStatus(
                    draw.getId(), TicketStatus.WAITING_FOR_DRAW, pageable);

            List<Ticket> tickets = ticketPage.getContent();
            if (tickets.isEmpty()) {
                break;
            }

            for (Ticket ticket : tickets) {
                int matchCount = ticket.getMatchCount();
                BigDecimal prizeAmount = prizePerTicket.getOrDefault(matchCount, BigDecimal.ZERO);
                ticket.setPrizeAmount(prizeAmount);
            }

            ticketRepository.saveAll(tickets);

            if (!ticketPage.hasNext()) {
                break;
            }
            page++;
        }
    }

    private Map<Integer, Integer> createPrizePercentageMap() {
        var prizes = lotteryConfig.getPrizes();
        return Map.of(
                5, prizes.getJackpotPercentage(),
                4, prizes.getHighPercentage(),
                3, prizes.getMediumPercentage(),
                2, prizes.getLowPercentage()
        );
    }

    private Map<Integer, BigDecimal> calculatePrizePerTicket(BigDecimal totalPool,
                                                             Map<Integer, Long> matchCountTotals,
                                                             Map<Integer, Integer> prizePercentages) {
        Map<Integer, BigDecimal> prizePerTicket = new HashMap<>();

        prizePercentages.forEach((matchCount, percentage) -> {
            Long ticketCount = matchCountTotals.get(matchCount);
            if (ticketCount == null || ticketCount == 0) {
                prizePerTicket.put(matchCount, BigDecimal.ZERO);
                return;
            }

            BigDecimal tierPool = totalPool
                    .multiply(BigDecimal.valueOf(percentage))
                    .divide(BigDecimal.valueOf(100));

            BigDecimal prize = tierPool.divide(
                    BigDecimal.valueOf(ticketCount),
                    2,
                    BigDecimal.ROUND_DOWN
            );

            prizePerTicket.put(matchCount, prize);
        });

        return prizePerTicket;
    }

    @Transactional(readOnly = true)
    public Draw getDrawById(Long drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Draw not found: " + drawId));
    }

    @Transactional(readOnly = true)
    public PageResponse<DrawResponse> completedDraw(Pageable pageable) {
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
    public DrawResponse currentActiveDraw() {
        return DrawResponse.fromEntity(activeDraw());
    }
}