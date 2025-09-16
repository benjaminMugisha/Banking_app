package com.benjamin.Banking_app.Loans;

public class LoanMapper {

    public static LoanDto mapToDto(Loan loan) {

        return loan == null ? null :
                LoanDto.builder()
                .loanId(loan.getLoanId())
                .principal(loan.getPrincipal())
                .remainingBalance(loan.getRemainingBalance())
                .amountToPayEachMonth(loan.getAmountToPayEachMonth())
                .startDate(loan.getStartDate())
                .nextPaymentDate(loan.getNextPaymentDate())
                .active(loan.isActive())
                .build();
    }
}
