package com.assesment.lottofun.service;

import com.assesment.lottofun.config.LotteryConfig;
import com.assesment.lottofun.controller.request.TicketPurchaseRequest;
import com.assesment.lottofun.controller.response.TicketBasicResponse;
import com.assesment.lottofun.controller.response.TicketDetailResponse;
import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.entity.TicketStatus;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.exception.DuplicateTicketException;
import com.assesment.lottofun.exception.InsufficientBalanceException;
import com.assesment.lottofun.exception.ResourceNotFoundException;
import com.assesment.lottofun.repository.TicketRepository;
import com.assesment.lottofun.util.LotteryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final DrawService drawService;
    private final UserService userService;
    private final LotteryConfig lotteryConfig;

    /**
     * Purchase a lottery ticket for the user
     */
    @Transactional
    public TicketBasicResponse purchaseTicket(String userEmail, TicketPurchaseRequest request) {
        var activeDraw = drawService.getDrawById(request.getDrawId());
        if (!activeDraw.canAcceptTickets()) {
            throw new BusinessException("The specified draw is no longer accepting tickets");
        }

        User user = userService.getUserByEmail(userEmail);
        BigDecimal ticketPrice = lotteryConfig.getTicket().getPrice();

        if (!user.hasSufficientBalance(ticketPrice)) {
            throw new InsufficientBalanceException("Insufficient balance. Required: " + ticketPrice + ", Available: " + user.getBalance());
        }

        List<Integer> sortedNumbers = LotteryUtils.ensureSortedNumbers(request.getSelectedNumbers());
        String numbersHash = LotteryUtils.generateNumbersHash(sortedNumbers);

        ticketRepository.findByUserIdAndDrawIdAndSelectedNumbersHash(user.getId(), activeDraw.getId(), numbersHash)
                .ifPresent(ticket -> {
                    throw new DuplicateTicketException("You have already purchased a ticket with these numbers for this draw");
                });

        Ticket ticket = Ticket.builder()
                .selectedNumbers(LotteryUtils.numbersToString(sortedNumbers))
                .selectedNumbersHash(numbersHash)
                .purchasePrice(ticketPrice)
                .status(TicketStatus.WAITING_FOR_DRAW)
                .userId(user.getId())
                .drawId(request.getDrawId())
                .draw(activeDraw)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        userService.deductBalance(userEmail, ticketPrice);

        return TicketBasicResponse.fromEntity(savedTicket);
    }



    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketDetails(String userEmail, Long ticketId) {
        User user = userService.getUserByEmail(userEmail);

        Ticket ticket = user.getTickets().stream()
                .filter(t -> t.getId().equals(ticketId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found for user: " + userEmail + " and ticket ID: " + ticketId)
                );
        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Ticket does not belong to the current user");
        }
        return TicketDetailResponse.fromEntity(ticket);
    }
}