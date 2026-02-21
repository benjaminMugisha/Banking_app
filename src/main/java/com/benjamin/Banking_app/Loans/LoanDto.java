package com.benjamin.Banking_app.Loans;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanDto {
    private Long loanId;
    private String loanOwner;
    private BigDecimal principal;
    private BigDecimal remainingBalance;
    private BigDecimal amountToPayEachMonth;
    private LocalDateTime startDate;
    private LocalDate nextPaymentDate;
    private boolean active;
}
