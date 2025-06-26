package com.assesment.lottofun.presentation.dto.response;

import com.assesment.lottofun.entity.Ticket;
import com.assesment.lottofun.util.NumberUtils;
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
    private BigDecimal prizeAmount;

    public static TicketDetailResponse fromEntity(Ticket ticket) {
        TicketDetailResponse response = new TicketDetailResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setSelectedNumbers(NumberUtils.stringToNumbersList(ticket.getSelectedNumbers()));
        response.setPurchasePrice(ticket.getPurchasePrice());
        response.setPurchaseTimestamp(ticket.getPurchaseTimestamp());
        response.setTicketStatus(ticket.getStatus().name());
        response.setDrawId(ticket.getDraw().getId());
        response.setMatchCount(ticket.getMatchCount());
        response.setPrizeAmount(ticket.getPrizeAmount());

        if (ticket.getDraw() != null) {
            var draw = ticket.getDraw();
            response.setDrawDate(draw.getDrawDate());
            response.setDrawStatus(draw.getStatus().name());
            response.setWinningNumbers(draw.getWinningNumbers() != null ?
                    NumberUtils.stringToNumbersList(draw.getWinningNumbers()) : null);
        }

        return response;
    }

}