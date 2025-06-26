package com.assesment.lottofun.presentation.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClaimTicketResponse {
    private Long ticketId;
    private String ticketNumber;
    private BigDecimal claimedAmount;
    private BigDecimal newBalance;
    private String message;

    public static ClaimTicketResponse create(Long ticketId, String ticketNumber,
                                             BigDecimal claimedAmount,
                                             BigDecimal newBalance) {
        ClaimTicketResponse response = new ClaimTicketResponse();
        response.setTicketId(ticketId);
        response.setTicketNumber(ticketNumber);
        response.setClaimedAmount(claimedAmount);
        response.setNewBalance(newBalance);
        response.setMessage("Prize claimed successfully! Amount added to your balance.");
        return response;
    }
}