package com.benjamin.Banking_app.Loans;

public interface LoanService {
    LoanResponse applyForLoan(LoanRequest loanRequest);
    LoanPageResponse getLoansOfAnAccount(int pageNo, int pageSize, String accountUsername);
    LoanResponse repayLoanEarly(Long loanId);
    LoanDto getLoanByLoanId(long loanId);
}
