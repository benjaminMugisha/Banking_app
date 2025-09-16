package com.benjamin.Banking_app.Accounts;

import java.math.BigDecimal;

public interface AccountService {

    AccountDto getAccountById(Long id);
    AccountDto deposit( BigDecimal amount);
    AccountDto withdraw(BigDecimal amount);
    AccountPageResponse getAllAccounts(int pageNo, int pageSize);
    void deleteAccount(Long id);
    AccountDto transfer(TransferRequest transferRequest);

}
