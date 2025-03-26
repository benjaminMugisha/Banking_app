package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;

public interface TransactionService {
    TransactionResponse findByAccountId(Long accountId, int pageNo, int pageSize);
    void recordTransaction(Account account, String type, double amount,
                           String description, String toAccount,
                           String fromAccount);

//    void recordLoanTransaction(Account account, String type,
//                               String monthlyRepayment, double remainingBalance, String s);
}