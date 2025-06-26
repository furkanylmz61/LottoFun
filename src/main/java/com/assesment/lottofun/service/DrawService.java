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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawRepository drawRepository;
    private final PrizeRulesConfig prizeRulesConfig;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public Draw getActiveDraw() {
        return getActive();
    }

    @Transactional
    public Draw newDraw() {
        drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN)
                .ifPresent(activeDraw -> {
                    throw new IllegalStateException("Active draw already exists: " + activeDraw.getId());
                });

        Draw newDraw = createNewDraw();

        return drawRepository.save(newDraw);
    }

    private Draw createNewDraw() {
        LocalDateTime scheduledDate = LocalDateTime.now()
                .plusMinutes(prizeRulesConfig.getDraw().getFrequencyMinutes());

        Draw newDraw = Draw.createNew(scheduledDate);
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

        draw.setAsFinalized();

        drawRepository.save(draw);
    }

    private Draw getActive() {
        return drawRepository.findFirstByStatusOrderByDrawDateAsc(DrawStatus.DRAW_OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No active draw available"));
    }

    @Transactional
    public Draw getLockDraw() {
        return drawRepository.getLockDraw(DrawStatus.DRAW_OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No active draw available"));
    }


    private void processTickets(Draw draw) {
        int batchSize = prizeRulesConfig.getDraw().getProcessingBatchSize();
        Page<Ticket> ticketPage;

        do {
            Pageable pageable = PageRequest.of(0, batchSize);
            ticketPage = ticketRepository.findByDrawIdAndStatus(draw.getId(), TicketStatus.WAITING_FOR_DRAW, pageable);

            List<Ticket> tickets = ticketPage.getContent();
            if (tickets.isEmpty()) break;

            for (Ticket ticket : tickets) {
                ticket.calculateResult(draw.getWinningNumbers());
            }

            ticketRepository.saveAll(tickets);

        } while (ticketPage.hasNext());
    }

    @Transactional(readOnly = true)
    public PageResponse<DrawResponse> filter(int page, int size, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "drawDate"));
        Page<Draw> drawsPage = drawRepository.findAll(pageable);
        Page<DrawResponse> responsePage = drawsPage.map(DrawResponse::fromEntity);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public void save(Draw draw) {
        drawRepository.save(draw);
    }
}