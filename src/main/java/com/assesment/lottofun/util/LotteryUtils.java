package com.assesment.lottofun.util;

import com.assesment.lottofun.exception.BusinessException;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.Set;

@UtilityClass
public class LotteryUtils {

    private static final SecureRandom RANDOM = new SecureRandom();


    public static void validateNumbers(Set<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("Numbers cannot be null or empty");
        }

        if (numbers.size() != 5) {
            throw new BusinessException("Exactly 5 numbers must be selected");
        }

        for (Integer number : numbers) {
            if (number == null || number < 1 || number > 49) {
                throw new BusinessException("All numbers must be between 1 and 49");
            }
        }
    }



}