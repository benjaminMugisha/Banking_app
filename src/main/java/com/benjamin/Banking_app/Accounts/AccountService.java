package com.benjamin.Banking_app.Accounts;

public interface AccountService {
    AccountDto createAccount(AccountDto account);
    AccountDto getAccountById(Long id);
    AccountDto deposit(Long id, double amount);
    AccountDto withdraw(Long id, double amount);
//    List<AccountDto> getAllAccounts();
    AccountResponse getAllAccounts(int pageNo, int pageSize);
    void deleteAccount(Long id);
    void transfer(TransferRequest transferRequest);
    DirectDebit createDirectDebit(Long fromId, Long toId, Double amount);
    void cancelDirectDebit(Long id);
//    List<DirectDebit> getAllDD();

}
