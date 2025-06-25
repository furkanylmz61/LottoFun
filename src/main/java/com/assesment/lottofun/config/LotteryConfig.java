package com.assesment.lottofun.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "lottery")
public class LotteryConfig {

    private Ticket ticket = new Ticket();
    private Draw draw = new Draw();
    private Prizes prizes = new Prizes();

    @Data
    public static class Ticket {
        private BigDecimal price = BigDecimal.valueOf(10.00);
        private Integer maxNumbers = 5;
        private Integer minNumber = 1;
        private Integer maxNumber = 49;
    }

    @Data
    public static class Draw {
        private Integer frequencyMinutes = 1;
    }

    @Data
    public static class Prizes {
        private Integer jackpotPercentage = 50;
        private Integer highPercentage = 25;
        private Integer mediumPercentage = 15;
        private Integer lowPercentage = 10;
    }
}