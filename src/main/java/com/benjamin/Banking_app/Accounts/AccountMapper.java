package com.benjamin.Banking_app.Accounts;

public class AccountMapper {
    public static AccountDto MapToAccountDto(Account account) {

        return account == null ? null :
                new AccountDto(account.getId(), account.getAccountUsername(), account.getBalance());
    }
}
