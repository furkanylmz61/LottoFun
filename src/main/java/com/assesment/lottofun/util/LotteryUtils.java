package com.assesment.lottofun.util;

import com.assesment.lottofun.exception.BusinessException;
import lombok.experimental.UtilityClass;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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


    public static String generateNumbersHash(Set<Integer> numbers) {
        // Sort numbers to ensure consistent hash regardless of input order
        List<Integer> sortedNumbers = numbers.stream()
                .sorted()
                .collect(Collectors.toList());

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = sortedNumbers.toString();
            byte[] hash = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
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



    public static String getPrizeTier(int matchCount) {
        return switch (matchCount) {
            case 5 -> "JACKPOT";
            case 4 -> "HIGH";
            case 3 -> "MEDIUM";
            case 2 -> "LOW";
            default -> "NO_PRIZE";
        };
    }

    /**
     * Validates if numbers are in ascending order (for consistency)
     */
    public static List<Integer> ensureSortedNumbers(HashSet<Integer> numbers) {
        return numbers.stream()
                .sorted()
                .collect(Collectors.toList());
    }
}