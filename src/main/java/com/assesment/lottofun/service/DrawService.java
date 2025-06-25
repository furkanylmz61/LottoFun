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
                .orElseThrow(() -> new ResourceNotFoundException("No active draw is currently available"));
    }

    @Transactional
    public Draw newDraw() {
        drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN)
                .ifPresent(activeDraw -> {
                    throw new IllegalStateException(
                            "An active draw already exists (id=" + activeDraw.getId() + ").");
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
    public void executePendingDraws() {
        List<Draw> pendingDraws = drawRepository
                .findByStatusAndDrawDateBefore(DrawStatus.DRAW_OPEN, LocalDateTime.now());

        if (pendingDraws.isEmpty()) {
            return;
        }
        pendingDraws.forEach(this::process);
    }


    @Transactional
    protected void process(Draw draw) {
        if (!draw.isEligibleForProcess()) {
            return;
        }

        // Draw'ı kapat
        draw.setAsClosed();
        drawRepository.save(draw);

        // Winning numbers'ı çek
        draw.setAsExtracted();
        drawRepository.save(draw);

        // Match count'lara göre ticket sayılarını topla
        Map<Integer, Long> matchCountTotals = new HashMap<>();

        // Pagination ile tüm tickets'ları işle ve match count'larını topla
        int page = 0;
        int size = 1000;
        boolean hasMoreTickets = true;

        while (hasMoreTickets) {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<Ticket> ticketPage = ticketRepository.findByDrawIdAndStatus(draw.getId(), TicketStatus.WAITING_FOR_DRAW, pageable);
            List<Ticket> tickets = ticketPage.getContent();

            if (tickets.isEmpty()) {
                hasMoreTickets = false;
                break;
            }

            // Bu sayfadaki tickets'ların match count'larını topla
            for (Ticket ticket : tickets) {
                int matchCount = ticket.getMatchCount();
                matchCountTotals.put(matchCount, matchCountTotals.getOrDefault(matchCount, 0L) + 1);
            }

            page++;
            hasMoreTickets = ticketPage.hasNext();
        }

        // Prize percentages map'ini oluştur
        Map<Integer, Integer> matchCountPricePercentage = createPrizePercentageMap();

        // Draw'ı finalize et
        draw.setAsFinalized(matchCountPricePercentage);
        drawRepository.save(draw);

        // Prize pool'u al
        BigDecimal totalPrizePool = draw.getTotalPrizePool();

        // Her match level için prize per ticket hesapla
        Map<Integer, BigDecimal> prizePerTicketByMatch = calculatePrizePerTicket(totalPrizePool, matchCountTotals, matchCountPricePercentage);

        // Şimdi tekrar pagination ile tickets'ları işle ve prize'ları ata
        page = 0;
        hasMoreTickets = true;

        while (hasMoreTickets) {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<Ticket> ticketPage = ticketRepository.findByDrawIdAndStatus(draw.getId(), TicketStatus.WAITING_FOR_DRAW, pageable);
            List<Ticket> tickets = ticketPage.getContent();

            if (tickets.isEmpty()) {
                hasMoreTickets = false;
                break;
            }

            // Bu sayfadaki tickets'lara prize'ları ata
            for (Ticket ticket : tickets) {
                int matchCount = ticket.getMatchCount();
                BigDecimal prizeAmount = prizePerTicketByMatch.getOrDefault(matchCount, BigDecimal.ZERO);
                ticket.setPrizeAmount(prizeAmount);
            }

            // Bu sayfadaki tickets'ları kaydet
            ticketRepository.saveAll(tickets);

            page++;
            hasMoreTickets = ticketPage.hasNext();
        }

        // Ödüllerin dağıtıldığını işaretle
        draw.markPrizesDistributed();
        drawRepository.save(draw);
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
        Map<Integer, BigDecimal> prizePerTicketByMatch = new HashMap<>();

        prizePercentages.forEach((matchCount, percentage) -> {
            Long ticketCount = matchCountTotals.get(matchCount);
            if (ticketCount == null || ticketCount == 0) {
                prizePerTicketByMatch.put(matchCount, BigDecimal.ZERO);
                return;
            }

            // Bu seviye için toplam ödül havuzu
            BigDecimal tierPool = totalPool
                    .multiply(BigDecimal.valueOf(percentage))
                    .divide(BigDecimal.valueOf(100));

            // Her ticket için ödül miktarı
            BigDecimal prizePerTicket = tierPool.divide(
                    BigDecimal.valueOf(ticketCount),
                    2, // scale
                    BigDecimal.ROUND_DOWN
            );

            prizePerTicketByMatch.put(matchCount, prizePerTicket);
        });

        return prizePerTicketByMatch;
    }
    @Transactional(readOnly = true)
    public Draw getDrawById(Long drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new ResourceNotFoundException("Draw not found with ID: " + drawId));
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
