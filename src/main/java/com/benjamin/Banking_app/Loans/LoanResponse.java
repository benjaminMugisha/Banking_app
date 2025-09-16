package com.benjamin.Banking_app.Loans;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanResponse {
    private String message;
    private LoanDto loanDto;
    private BigDecimal remainingLoanBalance;
    private BigDecimal amountToPayEachMonth;
    private Long loanId;
    private LocalDate nextPaymentDate;

    public LoanResponse(String message, LoanDto loanDto) {
        this.message = message;
        this.loanDto = loanDto;
    }

    public LoanResponse(String message) {
        this.message = message;
    }
}
