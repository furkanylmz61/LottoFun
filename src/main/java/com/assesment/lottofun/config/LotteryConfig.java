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
        private BigDecimal jackpot = BigDecimal.valueOf(1000000.00);
        private BigDecimal high = BigDecimal.valueOf(1000.00);
        private BigDecimal medium = BigDecimal.valueOf(100.00);
        private BigDecimal low = BigDecimal.valueOf(10.00);
    }

}