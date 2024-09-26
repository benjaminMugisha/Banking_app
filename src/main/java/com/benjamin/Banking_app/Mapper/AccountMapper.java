package com.benjamin.Banking_app.Mapper;


import com.benjamin.Banking_app.Dto.AccountDto;
import com.benjamin.Banking_app.Entity.Account;

public class AccountMapper {
    public static Account MapToAccount(AccountDto accountDto){

        if (accountDto == null) {
            return null; // Handle null input
        }

        Account account = new Account(accountDto.getId(),
                                      accountDto.getAccountUsername(),
                                      accountDto.getBalance()
        );
        return account;
    }
    public static AccountDto MapToAccountDto(Account account){

        if (account == null) {
            return null; // Handle null input
        }
        AccountDto accountDto = new AccountDto(account.getId(),
                account.getAccountUsername(),
                account.getBalance()
        );
        return accountDto;
    }
}
