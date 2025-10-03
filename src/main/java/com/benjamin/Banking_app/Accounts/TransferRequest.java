package com.benjamin.Banking_app.Accounts;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TransferRequest {
    @NotBlank
    private String toIban;

    @NotNull
    @DecimalMin(value = "1", message = "amount must be at least €1")
    private BigDecimal amount;
}
