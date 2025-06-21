package com.assesment.lottofun.dto.response;

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
}
