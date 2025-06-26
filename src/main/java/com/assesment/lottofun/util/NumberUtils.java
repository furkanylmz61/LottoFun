package com.assesment.lottofun.util;

import com.assesment.lottofun.exception.BusinessException;

import java.util.*;
import java.util.stream.Collectors;

public class NumberUtils {

    private NumberUtils() {
    }

    private static final int REQUIRED_NUMBER_COUNT = 5;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 49;
    private static final String DELIMITER = ",";


    public static String numbersToString(Set<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "";
        }
        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));
    }

    public static Set<Integer> stringToNumbers(String numbersString) {
        if (numbersString == null || numbersString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(numbersString.split(DELIMITER))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }


    public static List<Integer> stringToNumbersList(String numbersString) {
        if (numbersString == null || numbersString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(numbersString.split(DELIMITER))
                .map(String::trim)
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
    }

    public static void validateLotteryNumbers(Set<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("Numbers cannot be null or empty");
        }

        if (numbers.size() != REQUIRED_NUMBER_COUNT) {
            throw new BusinessException(
                    String.format("Exactly %d numbers must be selected, but got %d",
                            REQUIRED_NUMBER_COUNT, numbers.size())
            );
        }

        for (Integer number : numbers) {
            if (number == null) {
                throw new BusinessException("Number cannot be null");
            }
            if (number < MIN_NUMBER || number > MAX_NUMBER) {
                throw new BusinessException(
                        String.format("All numbers must be between %d and %d, but got %d",
                                MIN_NUMBER, MAX_NUMBER, number)
                );
            }
        }
    }

    public static int calculateMatches(String selectedNumbers, String winningNumbers) {
        Set<Integer> selectedSet = stringToNumbers(selectedNumbers);
        Set<Integer> winningSet = stringToNumbers(winningNumbers);

        selectedSet.retainAll(winningSet);
        return selectedSet.size();
    }

    public static boolean isValidLotteryNumbers(Set<Integer> numbers) {
        try {
            validateLotteryNumbers(numbers);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }



}