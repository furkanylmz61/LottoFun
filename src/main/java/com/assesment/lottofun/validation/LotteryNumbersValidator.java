package com.assesment.lottofun.validation;

import com.assesment.lottofun.util.LotteryUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class LotteryNumbersValidator implements ConstraintValidator<ValidLotteryNumbers, Set<Integer>> {

    @Override
    public boolean isValid(Set<Integer> value, ConstraintValidatorContext context) {
        try {
            LotteryUtils.validateNumbers(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
