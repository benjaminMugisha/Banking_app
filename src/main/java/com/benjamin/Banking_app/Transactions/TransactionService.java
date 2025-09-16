package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;

import java.math.BigDecimal;

public interface TransactionService {
    TransactionResponse transactions(int pageNo, int pageSize, String accountUsername);
    void recordTransaction(Account account, TransactionType type, BigDecimal amount,
                            Account toAccount);
}
