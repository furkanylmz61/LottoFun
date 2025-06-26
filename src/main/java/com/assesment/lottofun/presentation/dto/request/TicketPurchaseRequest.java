package com.assesment.lottofun.presentation.dto.request;

import com.assesment.lottofun.presentation.validation.ValidLotteryNumbers;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class TicketPurchaseRequest {

    @NotNull(message = "Selected numbers are required")
    @Size(min = 5, max = 5, message = "Exactly 5 numbers must be selected")
    @ValidLotteryNumbers
    private Set<Integer> selectedNumbers;

}