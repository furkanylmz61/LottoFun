package com.assesment.lottofun.presentation.validation;

import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.service.DrawService;
import com.assesment.lottofun.service.UserService;
import com.assesment.lottofun.util.NumberUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

public class LotteryNumbersValidator implements ConstraintValidator<ValidLotteryNumbers, Set<Integer>> {

    private final UserService userService;
    private final DrawService drawService;

    public LotteryNumbersValidator(UserService userService, DrawService drawService) {
        this.userService = userService;
        this.drawService = drawService;
    }

    @Override
    public boolean isValid(Set<Integer> value, ConstraintValidatorContext context) {
        try {
            NumberUtils.validateLotteryNumbers(value);

            String userEmail = getCurrentUserEmail();
            if (userEmail != null) {
                User user = userService.getUserByEmail(userEmail);
                Long activeDrawId = drawService.getActiveDraw().getId();

                if (user.hasTicketAlready(activeDrawId, value)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                                    "You have already purchased a ticket with these numbers for the current draw")
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}