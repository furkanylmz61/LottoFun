package com.assesment.lottofun.util;

import com.assesment.lottofun.exception.BusinessException;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

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

        // Check range (1-49)
        for (Integer number : numbers) {
            if (number == null || number < 1 || number > 49) {
                throw new BusinessException("All numbers must be between 1 and 49");
            }
        }
    }

    public static String numbersToString(Set<Integer> numbers) {
        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public static Set<Integer> stringToNumbers(String numbersString) {
        if (numbersString == null || numbersString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(numbersString.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    public static List<Integer> stringToNumbersList(String numbersString) {
        if (numbersString == null || numbersString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(numbersString.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }

}