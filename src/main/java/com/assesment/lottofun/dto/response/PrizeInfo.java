package com.assesment.lottofun.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrizeInfo {
    private String tier;
    private Integer winnerCount;
    private BigDecimal totalPaid;
    private BigDecimal prizePerWinner;
}
