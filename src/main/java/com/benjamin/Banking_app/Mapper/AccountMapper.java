package com.benjamin.Banking_app.Mapper;


import com.benjamin.Banking_app.AccountDTO.AccountDto;
import com.benjamin.Banking_app.Entity.Account;

//a mapper class is often used to map data between different representations or layers of the application, such
// as between domain objects, DTOs (Data Transfer Objects), and database entities.
public class AccountMapper {
    //turn accountdto into an account jpa entity
    public static Account MapToAccount(AccountDto accountDto){
        Account account = new Account(accountDto.getId(),
                                      accountDto.getAccountUsername(),
                                      accountDto.getBalance()
        );
        return account;
    }
    //turn account jpa into an account dto
    public static AccountDto MapToAccountDto(Account account){
        AccountDto accountDto = new AccountDto(account.getId(),
                account.getAccountUsername(),
                account.getBalance()
        );
        return accountDto;
    }
}
