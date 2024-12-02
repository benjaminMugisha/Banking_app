package com.benjamin.Banking_app.Loans;

import java.util.List;

public interface LoanService {

    LoanResponse applyForLoan(LoanRequest loanRequest);
    List<Loan> getAllLoans();
    List<Loan> getLoansByAccountId(long accountId);
    LoanResponse repayLoanEarly(Long loanId, double paymentAmount);
    void processMonthlyRepayments();
    double calculateMonthlyInstallment(double principal, int monthsToRepay);
    void deleteLoan(long loanId);
    Loan getLoanByLoanId(long loanId);
    boolean isLoanAffordable(double yearlyIncome, double estimatedYearlyPayment, long accountId);

}

