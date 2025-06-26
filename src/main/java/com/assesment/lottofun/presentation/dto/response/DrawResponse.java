package com.assesment.lottofun.presentation.dto.response;

import com.assesment.lottofun.entity.Draw;
import com.assesment.lottofun.util.NumberUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DrawResponse {
    private Long id;
    private LocalDateTime drawDate;
    private String status;
    private List<Integer> winningNumbers;
    private LocalDateTime executedAt;
    private LocalDateTime prizesDistributedAt;
    private LocalDateTime createdAt;

    public static DrawResponse fromEntity(Draw draw) {
        DrawResponse response = new DrawResponse();
        response.setId(draw.getId());
        response.setDrawDate(draw.getDrawDate());
        response.setStatus(draw.getStatus().name());
        response.setExecutedAt(draw.getExecutedAt());
        response.setPrizesDistributedAt(draw.getPrizesDistributedAt());
        response.setCreatedAt(draw.getCreatedAt());

        if (draw.getWinningNumbers() != null && !draw.getWinningNumbers().isBlank()) {
            response.setWinningNumbers(NumberUtils.stringToNumbersList(draw.getWinningNumbers()));
        }

        return response;
    }
}