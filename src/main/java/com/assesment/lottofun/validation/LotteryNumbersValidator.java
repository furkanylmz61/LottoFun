package com.assesment.lottofun.validation;

import com.assesment.lottofun.util.LotteryUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;

public class LotteryNumbersValidator implements ConstraintValidator<ValidLotteryNumbers, HashSet<Integer>> {

    @Override
    public boolean isValid(HashSet<Integer> value, ConstraintValidatorContext context) {
        try {
            LotteryUtils.validateNumbers(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
