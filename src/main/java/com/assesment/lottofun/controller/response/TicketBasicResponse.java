package com.assesment.lottofun.controller.response;

import com.assesment.lottofun.entity.Ticket;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketBasicResponse {
    private Long id;
    private String ticketNumber;
    private List<Integer> selectedNumbers;
    private BigDecimal purchasePrice;
    private LocalDateTime purchaseTimestamp;
    private String ticketStatus;
    private Long drawId;
    private LocalDateTime drawDate;

    public static TicketBasicResponse fromEntity(Ticket ticket) {
        TicketBasicResponse response = new TicketBasicResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setSelectedNumbers(ticket.getSelectedNumbers() != null ?
                List.of(ticket.getSelectedNumbers().split(","))
                        .stream()
                        .map(Integer::parseInt)
                        .toList() : null);
        response.setPurchasePrice(ticket.getPurchasePrice());
        response.setPurchaseTimestamp(ticket.getPurchaseTimestamp());
        response.setTicketStatus(ticket.getStatus().name());
        response.setDrawId(ticket.getDraw().getId());

        if (ticket.getDraw() != null) {
            response.setDrawDate(ticket.getDraw().getDrawDate());
        }

        return response;
    }

}
