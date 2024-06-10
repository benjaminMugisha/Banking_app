package com.benjamin.Banking_app.Service;


import com.benjamin.Banking_app.AccountDTO.AccountDto;

import java.util.List;

public interface AccountService {
    AccountDto createAccount(AccountDto account);
    AccountDto getAccountById(Long id);

    AccountDto deposit(Long id, double amount);

    AccountDto withdraw(Long id, double amount);

    List<AccountDto> getAllAccounts();

    void deleteAccount(Long id);

}
