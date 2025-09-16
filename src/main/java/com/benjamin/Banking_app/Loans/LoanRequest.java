package com.benjamin.Banking_app.Loans;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoanRequest {

    @NotNull
    @DecimalMin(value = "100", message = "income must be above €50 to apply for a loan")
    BigDecimal income;

    @NotNull
    @DecimalMin(value = "10", message = "loan must be at least €10")
    BigDecimal principal;

    @Min(value = 1, message = "Months to repay must be at least 1")
    int monthsToRepay;
}
