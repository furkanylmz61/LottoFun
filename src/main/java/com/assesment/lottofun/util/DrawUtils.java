package com.assesment.lottofun.util;

import java.security.SecureRandom;
import java.util.*;

public class DrawUtils {

    private DrawUtils() {
    }
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int REQUIRED_NUMBERS = 5;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 49;

    public static String generateWinningNumbers() {
        Set<Integer> winningNumbers = new HashSet<>();

        while (winningNumbers.size() < REQUIRED_NUMBERS) {
            int number = RANDOM.nextInt(MAX_NUMBER) + MIN_NUMBER; // 1-49
            winningNumbers.add(number);
        }

        return NumberUtils.numbersToString(winningNumbers);
    }

}
