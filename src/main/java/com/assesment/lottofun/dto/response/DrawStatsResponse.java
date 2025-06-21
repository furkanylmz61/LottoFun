package com.assesment.lottofun.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DrawStatsResponse {
    private DrawResponse draw;
    private Integer totalTickets;
    private Integer totalWinners;
    private List<TicketDetailResponse> winners;
    private Map<Integer, Integer> winnersByMatchCount;
    private List<PrizeInfo> prizeDistribution;
}