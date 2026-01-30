package com.benjamin.Banking_app.DirectDebit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DirectDebitRequest {
    @NotBlank(message = "to account username cannot be empty")
    private String toIban;

    @DecimalMin(value = "1.0", message = "amount can't be less than 1")
    private BigDecimal amount;
}
