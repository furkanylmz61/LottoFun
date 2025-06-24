package com.assesment.lottofun.controller.response;

import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.util.LotteryUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketDetailResponse {
    private Long id;
    private String ticketNumber;
    private List<Integer> selectedNumbers;
    private BigDecimal purchasePrice;
    private LocalDateTime purchaseTimestamp;
    private String ticketStatus;
    private Long drawId;
    private LocalDateTime drawDate;
    private String drawStatus;
    private List<Integer> winningNumbers;
    private Integer matchCount;
    private String matchedNumbers;
    private BigDecimal prizeAmount;
    private LocalDateTime claimedAt;
    private String prizeType;

    public static TicketDetailResponse fromEntity(Ticket ticket) {
        TicketDetailResponse response = new TicketDetailResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setSelectedNumbers(LotteryUtils.stringToNumbers(ticket.getSelectedNumbers()));
        response.setPurchasePrice(ticket.getPurchasePrice());
        response.setPurchaseTimestamp(ticket.getPurchaseTimestamp());
        response.setTicketStatus(ticket.getStatus().name());
        response.setDrawId(ticket.getDrawId());
        response.setMatchCount(ticket.getMatchCount());
        response.setMatchedNumbers(ticket.getMatchedNumbers());
        response.setPrizeAmount(ticket.getPrizeAmount());
        response.setClaimedAt(ticket.getClaimedAt());

        if (ticket.getMatchCount() != null) {
            response.setPrizeType(LotteryUtils.getPrizeTier(ticket.getMatchCount()));
        }

        if (ticket.getDraw() != null) {
            var draw = ticket.getDraw();
            response.setDrawDate(draw.getDrawDate());
            response.setDrawStatus(draw.getStatus().name());
            response.setWinningNumbers(draw.getWinningNumbers() != null ?
                    LotteryUtils.stringToNumbers(draw.getWinningNumbers()) : null);
        }

        return response;
    }

}