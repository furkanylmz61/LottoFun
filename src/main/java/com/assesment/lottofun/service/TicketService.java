package com.assesment.lottofun.service;

import com.assesment.lottofun.config.PrizeRulesConfig;
import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.infrastructure.repository.TicketRepository;
import com.assesment.lottofun.presentation.dto.request.TicketPurchaseRequest;
import com.assesment.lottofun.presentation.dto.response.TicketBasicResponse;
import com.assesment.lottofun.presentation.dto.response.TicketDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final DrawService drawService;
    private final UserService userService;
    private final PrizeRulesConfig prizeRulesConfig;

    @Transactional
    public TicketBasicResponse purchase(String userEmail, TicketPurchaseRequest request) {
        Draw activeDraw = drawService.getActiveDraw();

        if (!activeDraw.canAcceptTickets()) {
            throw new BusinessException("The current active draw is no longer accepting tickets");
        }

        User user = userService.getUserByEmail(userEmail);
        BigDecimal ticketPrice = prizeRulesConfig.getTicket().getPrice();


        user.deductBalance(ticketPrice);
        userService.save(user);

        Set<Integer> selectedNumbers = request.getSelectedNumbers();

        Ticket ticket = Ticket.createNew(
                user,
                activeDraw,
                selectedNumbers,
                ticketPrice
        );

        Ticket saved = ticketRepository.save(ticket);
        drawService.save(activeDraw);

        return TicketBasicResponse.fromEntity(saved);
    }


    @Transactional(readOnly = true)
    public TicketDetailResponse ticketDetail(String userEmail, Long ticketId) {
        User user = userService.getUserByEmail(userEmail);

        Ticket ticket = user.getTickets().stream()
                .filter(t -> t.getId().equals(ticketId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found for user: " + userEmail + " and ticket ID: " + ticketId)
                );

        return TicketDetailResponse.fromEntity(ticket);
    }
}