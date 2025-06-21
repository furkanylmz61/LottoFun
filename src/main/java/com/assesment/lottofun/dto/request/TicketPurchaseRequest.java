package com.assesment.lottofun.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TicketPurchaseRequest {

    @NotNull(message = "Selected numbers are required")
    @Size(min = 5, max = 5, message = "Exactly 5 numbers must be selected")
    private List<Integer> selectedNumbers;
    private Long drawId;

}