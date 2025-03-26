package com.benjamin.Banking_app.Loans;

import java.util.List;

public interface LoanService {

    LoanResponse applyForLoan(LoanRequest loanRequest);
//    List<Loan> getAllLoans();
    LoanPageResponse getAllLoans(int pageNo, int pageSize);
    List<Loan> getLoansByAccountId(long accountId);
    double calculateMonthlyInstallment(double principal, int monthsToRepay);
    void deleteLoan(long loanId);
    Loan getLoanByLoanId(long loanId);
    boolean isLoanAffordable(double yearlyIncome, double estimatedYearlyPayment, long accountId);
    LoanResponse processMonthlyRepayment(Long loanId, double amount);
    LoanResponse repayLoanEarly(Long loanId);
}

