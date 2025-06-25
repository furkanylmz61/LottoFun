package com.assesment.lottofun.controller.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClaimTicketResponse {
    private Long ticketId;
    private String ticketNumber;
    private BigDecimal claimedAmount;
    private LocalDateTime claimedAt;
    private BigDecimal newBalance;
    private String message;

    public static ClaimTicketResponse create(Long ticketId, String ticketNumber,
                                             BigDecimal claimedAmount, LocalDateTime claimedAt,
                                             BigDecimal newBalance) {
        ClaimTicketResponse response = new ClaimTicketResponse();
        response.setTicketId(ticketId);
        response.setTicketNumber(ticketNumber);
        response.setClaimedAmount(claimedAmount);
        response.setClaimedAt(claimedAt);
        response.setNewBalance(newBalance);
        response.setMessage("Prize claimed successfully! Amount added to your balance.");
        return response;
    }
}