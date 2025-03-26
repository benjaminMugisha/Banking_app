package com.benjamin.Banking_app.Accounts;


import java.util.List;

//implemented by AccountServiceImpl.java
public interface AccountService {
    AccountDto createAccount(AccountDto account);
    AccountDto getAccountById(Long id);
    AccountDto deposit(Long id, double amount);
    AccountDto withdraw(Long id, double amount);
//    List<AccountDto> getAllAccounts();
    AccountResponse getAllAccounts(int pageNo, int pageSize);
    void deleteAccount(Long id);
    void transfer(TransferRequest transferRequest);
    void directDebit(TransferRequest transferRequest);
}
