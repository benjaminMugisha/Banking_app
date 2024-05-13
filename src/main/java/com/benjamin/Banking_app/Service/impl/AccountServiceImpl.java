package com.benjamin.Banking_app.Service.impl;


import com.benjamin.Banking_app.AccountDTO.AccountDto;
import com.benjamin.Banking_app.Entity.Account;
import com.benjamin.Banking_app.Mapper.AccountMapper;
import com.benjamin.Banking_app.Repository.AccountRepo;
import com.benjamin.Banking_app.Service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo; // create a dependency of AccountRepo class

    //we don't need @AutoWired because if spring finds a single constructor in our spring bean,
    // spring will automatically inject the bean for us.
    public AccountServiceImpl(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override //we need to convert accountDto into account jpa then we'll save that account jpa into a db.
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.MapToAccount(accountDto); // that why this method is static
        Account savedAccount = accountRepo.save(account); //saving the account
        return AccountMapper.MapToAccountDto(savedAccount);//return dto because entity has sensitive information
    }

    @Override
    public AccountDto getAccountById(Long id) {
         Account acc = accountRepo.findById(id)
         .orElseThrow(() -> new RuntimeException("account does not exist"));
         return AccountMapper.MapToAccountDto(acc);
    }

    @Override
    public AccountDto deposit(Long id, double amount) {
        //first check if the id given does exist as an account
        Account account = accountRepo.findById(id).get();
                //.orElseThrow(() -> new RuntimeException("account does not exist"));

        //update the balance
        double total = account.getBalance() + amount;
        account.setBalance(total);
        Account savedAccount = accountRepo.save(account);
        return AccountMapper.MapToAccountDto(savedAccount);
    }
    @Override
    public AccountDto withdraw(Long id, double amount) {

        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        if(amount > account.getBalance())
            throw new RuntimeException("insufficient funds brokie");

        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepo.save(account);

        return AccountMapper.MapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepo.findAll();
        return accounts.stream().map((account) -> AccountMapper.MapToAccountDto(account)).
                collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        accountRepo.deleteById(id);
    }
}