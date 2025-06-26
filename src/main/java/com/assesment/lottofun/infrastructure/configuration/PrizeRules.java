package com.assesment.lottofun.infrastructure.configuration;

import com.assesment.lottofun.config.PrizeRulesConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PrizeRules {

    private static Map<Integer, BigDecimal> staticPrizeMap;


    public PrizeRules(PrizeRulesConfig prizeRulesConfig) {
        initStaticMap(prizeRulesConfig.getPrizes());
    }

    private void initStaticMap(PrizeRulesConfig.Prizes prizes) {
        staticPrizeMap = Map.of(
                5, prizes.getJackpot(),
                4, prizes.getHigh(),
                3, prizes.getMedium(),
                2, prizes.getLow()
        );
    }

    public static BigDecimal getPrize(int matchCount) {
        return staticPrizeMap.getOrDefault(matchCount, BigDecimal.ZERO);
    }

}
