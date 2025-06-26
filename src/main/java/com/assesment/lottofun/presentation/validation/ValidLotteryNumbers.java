package com.assesment.lottofun.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LotteryNumbersValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLotteryNumbers {
    String message() default "Invalid lottery numbers";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
