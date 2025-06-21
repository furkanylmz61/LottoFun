package com.assesment.lottofun.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DrawResponse {
    private Long id;
    private LocalDateTime drawDate;
    private String status;
    private List<Integer> winningNumbers;
    private Integer totalTickets;
    private BigDecimal totalPrizePool;
    private LocalDateTime executedAt;
    private LocalDateTime prizesDistributedAt;
    private LocalDateTime createdAt;
}