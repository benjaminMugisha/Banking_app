package com.benjamin.Banking_app.Accounts;

public class AccountMapper {
    public static Account MapToAccount(AccountDto accountDto){

        if (accountDto == null) {
            return null; // Handle null input
        }

        return new Account(accountDto.getId(),
                                      accountDto.getAccountUsername(),
                                      accountDto.getBalance()
        );
    }
    public static AccountDto MapToAccountDto(Account account){

        if (account == null) {
            return null;
        }
        return new AccountDto(account.getId(),
                account.getAccountUsername(),
                account.getBalance()
        );
    }
}