package com.assesment.lottofun.util;

import java.util.*;
import java.util.stream.Collectors;

public class DrawUtil {

    private static final Random RANDOM = new Random();


    public static String generateWinningNumbers() {
        Set<Integer> winningNumbers = new HashSet<>();
        while (winningNumbers.size() < 5) {
            int number = RANDOM.nextInt(49) + 1;
            winningNumbers.add(number);
        }
        return winningNumbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
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
