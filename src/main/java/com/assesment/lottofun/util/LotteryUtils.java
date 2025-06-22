package com.assesment.lottofun.util;

import com.assesment.lottofun.entity.TicketStatus;
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

    /**
     * Validates lottery numbers according to rules:
     * - Exactly 5 numbers
     * - All numbers between 1-49
     * - All numbers unique
     */
    public static void validateNumbers(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("Numbers cannot be null or empty");
        }

        if (numbers.size() != 5) {
            throw new BusinessException("Exactly 5 numbers must be selected");
        }

        // Check for duplicates
        Set<Integer> uniqueNumbers = new HashSet<>(numbers);
        if (uniqueNumbers.size() != numbers.size()) {
            throw new BusinessException("All numbers must be unique");
        }

        // Check range (1-49)
        for (Integer number : numbers) {
            if (number == null || number < 1 || number > 49) {
                throw new BusinessException("All numbers must be between 1 and 49");
            }
        }
    }

    /**
     * Generates a hash for selected numbers to prevent duplicate tickets
     */
    public static String generateNumbersHash(List<Integer> numbers) {
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


    public static String numbersToString(List<Integer> numbers) {
        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }


    public static List<Integer> stringToNumbers(String numbersString) {
        if (numbersString == null || numbersString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(numbersString.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }


    public static List<Integer> generateWinningNumbers() {
        Set<Integer> winningNumbers = new HashSet<>();

        while (winningNumbers.size() < 5) {
            int number = RANDOM.nextInt(49) + 1; // 1-49
            winningNumbers.add(number);
        }

        return winningNumbers.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Calculates how many numbers match between selected and winning numbers
     */
    public static int calculateMatches(List<Integer> selectedNumbers, List<Integer> winningNumbers) {
        Set<Integer> selected = new HashSet<>(selectedNumbers);
        Set<Integer> winning = new HashSet<>(winningNumbers);

        selected.retainAll(winning); // Intersection
        return selected.size();
    }

    /**
     * Determines ticket status based on match count
     */
    public static TicketStatus determineTicketStatus(int matchCount) {
        return matchCount >= 2 ? TicketStatus.WON : TicketStatus.NOT_WON;
    }

    /**
     * Gets prize tier based on match count
     */
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
    public static List<Integer> ensureSortedNumbers(List<Integer> numbers) {
        return numbers.stream()
                .sorted()
                .collect(Collectors.toList());
    }
}