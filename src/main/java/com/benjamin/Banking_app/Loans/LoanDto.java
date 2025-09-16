package com.benjamin.Banking_app.Loans;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanDto {
    private Long loanId;
    private BigDecimal principal;
    private BigDecimal remainingBalance;
    private BigDecimal amountToPayEachMonth;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private boolean active;
}

