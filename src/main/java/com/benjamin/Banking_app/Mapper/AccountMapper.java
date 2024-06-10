package com.benjamin.Banking_app.Mapper;


import com.benjamin.Banking_app.AccountDTO.AccountDto;
import com.benjamin.Banking_app.Entity.Account;

public class AccountMapper {
    public static Account MapToAccount(AccountDto accountDto){
        Account account = new Account(accountDto.getId(),
                                      accountDto.getAccountUsername(),
                                      accountDto.getBalance()
        );
        return account;
    }
    public static AccountDto MapToAccountDto(Account account){
        AccountDto accountDto = new AccountDto(account.getId(),
                account.getAccountUsername(),
                account.getBalance()
        );
        return accountDto;
    }
}
