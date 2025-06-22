package com.assesment.lottofun.service;

import com.assesment.lottofun.config.LotteryConfig;
import com.assesment.lottofun.dto.request.TicketPurchaseRequest;
import com.assesment.lottofun.dto.response.PageResponse;
import com.assesment.lottofun.dto.response.TicketBasicResponse;
import com.assesment.lottofun.dto.response.TicketDetailResponse;
import com.assesment.lottofun.entity.Draw;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        log.info("Processing ticket purchase for user: {} with numbers: {}", userEmail, request.getSelectedNumbers());

        // Validate selected numbers
        LotteryUtils.validateNumbers(request.getSelectedNumbers());

        // Get or create current active draw
        Draw activeDraw;
        if (request.getDrawId() != null) {
            activeDraw = drawService.getDrawById(request.getDrawId());
            if (!activeDraw.canAcceptTickets()) {
                throw new BusinessException("The specified draw is no longer accepting tickets");
            }
        } else {
            activeDraw = drawService.getCurrentActiveDraw();
        }

        // Get user
        User user = userService.getUserByEmail(userEmail);

        // Check user balance
        BigDecimal ticketPrice = lotteryConfig.getTicket().getPrice();
        if (!user.hasSufficientBalance(ticketPrice)) {
            throw new InsufficientBalanceException("Insufficient balance. Required: " + ticketPrice + ", Available: " + user.getBalance());
        }

        // Sort numbers for consistency
        List<Integer> sortedNumbers = LotteryUtils.ensureSortedNumbers(request.getSelectedNumbers());

        // Generate hash to prevent duplicate tickets
        String numbersHash = LotteryUtils.generateNumbersHash(sortedNumbers);

        // Check for duplicate ticket
        Optional<Ticket> existingTicket = ticketRepository.findByUserIdAndDrawIdAndSelectedNumbersHash(
                user.getId(), activeDraw.getId(), numbersHash);

        if (existingTicket.isPresent()) {
            throw new DuplicateTicketException("You have already purchased a ticket with these numbers for this draw");
        }

        // Create ticket
        Ticket ticket = Ticket.builder()
                .selectedNumbers(LotteryUtils.numbersToString(sortedNumbers))
                .selectedNumbersHash(numbersHash)
                .purchasePrice(ticketPrice)
                .status(TicketStatus.WAITING_FOR_DRAW)
                .user(user)
                .draw(activeDraw)
                .build();

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Deduct balance from user
        userService.updateUserBalance(userEmail, ticketPrice, "deduct");

        // Register ticket purchase in draw
        drawService.registerTicketPurchase(activeDraw.getId(), ticketPrice);

        log.info("Ticket purchased successfully - ID: {}, User: {}, Draw: {}",
                savedTicket.getId(), userEmail, activeDraw.getId());

        return convertToBasicResponse(savedTicket);
    }

    /**
     * Get user's tickets with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<TicketBasicResponse> getUserTickets(String userEmail, Pageable pageable) {
        log.debug("Getting tickets for user: {} with pagination: {}", userEmail, pageable);

        User user = userService.getUserByEmail(userEmail);

        Page<Ticket> ticketsPage = ticketRepository.findByUserIdOrderByPurchaseTimestampDesc(user.getId(), pageable);

        Page<TicketBasicResponse> responsePage = ticketsPage.map(this::convertToBasicResponse);

        return PageResponse.from(responsePage);
    }

    /**
     * Get detailed ticket information by ID
     */
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketDetails(String userEmail, Long ticketId) {
        log.debug("Getting ticket details for user: {} and ticket: {}", userEmail, ticketId);

        User user = userService.getUserByEmail(userEmail);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        // Verify ticket belongs to user
        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Ticket does not belong to the current user");
        }

        return convertToDetailResponse(ticket);
    }

    /**
     * Get tickets for a specific draw
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsForDraw(Long drawId) {
        return ticketRepository.findByDrawId(drawId);
    }

    /**
     * Get tickets for user and specific draw
     */
    @Transactional(readOnly = true)
    public List<TicketDetailResponse> getUserTicketsForDraw(String userEmail, Long drawId) {
        log.debug("Getting tickets for user: {} and draw: {}", userEmail, drawId);

        User user = userService.getUserByEmail(userEmail);

        List<Ticket> tickets = ticketRepository.findByUserIdAndDrawId(user.getId(), drawId);

        return tickets.stream()
                .map(this::convertToDetailResponse)
                .toList();
    }

    /**
     * Convert Ticket entity to TicketBasicResponse DTO
     */
    private TicketBasicResponse convertToBasicResponse(Ticket ticket) {
        TicketBasicResponse response = new TicketBasicResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setSelectedNumbers(LotteryUtils.stringToNumbers(ticket.getSelectedNumbers()));
        response.setPurchasePrice(ticket.getPurchasePrice());
        response.setPurchaseTimestamp(ticket.getPurchaseTimestamp());
        response.setTicketStatus(ticket.getStatus().name());
        response.setDrawId(ticket.getDraw().getId());
        response.setDrawDate(ticket.getDraw().getDrawDate());

        return response;
    }

    /**
     * Convert Ticket entity to TicketDetailResponse DTO
     */
    private TicketDetailResponse convertToDetailResponse(Ticket ticket) {
        TicketDetailResponse response = new TicketDetailResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setSelectedNumbers(LotteryUtils.stringToNumbers(ticket.getSelectedNumbers()));
        response.setPurchasePrice(ticket.getPurchasePrice());
        response.setPurchaseTimestamp(ticket.getPurchaseTimestamp());
        response.setTicketStatus(ticket.getStatus().name());
        response.setDrawId(ticket.getDraw().getId());
        response.setDrawDate(ticket.getDraw().getDrawDate());
        response.setDrawStatus(ticket.getDraw().getStatus().name());
        response.setMatchCount(ticket.getMatchCount());
        response.setPrizeAmount(ticket.getPrizeAmount());
        response.setClaimedAt(ticket.getClaimedAt());

        // Add winning numbers if available
        if (ticket.getDraw().getWinningNumbers() != null && !ticket.getDraw().getWinningNumbers().trim().isEmpty()) {
            response.setWinningNumbers(LotteryUtils.stringToNumbers(ticket.getDraw().getWinningNumbers()));
        }

        // Add prize type
        if (ticket.getMatchCount() != null) {
            response.setPrizeType(LotteryUtils.getPrizeTier(ticket.getMatchCount()));
        }

        return response;
    }

    /**
     * Check if user can purchase ticket for current draw
     */
    @Transactional(readOnly = true)
    public boolean canUserPurchaseTicket(String userEmail, List<Integer> selectedNumbers) {
        try {
            LotteryUtils.validateNumbers(selectedNumbers);

            User user = userService.getUserByEmail(userEmail);
            BigDecimal ticketPrice = lotteryConfig.getTicket().getPrice();

            return user.hasSufficientBalance(ticketPrice);
        } catch (Exception e) {
            return false;
        }
    }
}