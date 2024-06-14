package com.benjamin.Banking_app.Service.impl;


import com.benjamin.Banking_app.Dto.AccountDto;
import com.benjamin.Banking_app.Dto.TransferRequest;
import com.benjamin.Banking_app.Entity.Account;
import com.benjamin.Banking_app.Mapper.AccountMapper;
import com.benjamin.Banking_app.Repository.AccountRepo;
import com.benjamin.Banking_app.Service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;

    public AccountServiceImpl(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.MapToAccount(accountDto);
        Account savedAccount = accountRepo.save(account);
        return AccountMapper.MapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account acc = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));
        return AccountMapper.MapToAccountDto(acc);
    }

    @Override
    public AccountDto deposit(Long id, double amount) {
        Account account = accountRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("account does not exist"));

        //update the balance
        double total = account.getBalance() + amount;
        account.setBalance(total);
        Account savedAccount = accountRepo.save(account);
        return AccountMapper.MapToAccountDto(savedAccount);
    }

    @Transactional
    public void transfer(TransferRequest transferRequest) {
        // Retrieve accounts
        Account fromAccount = accountRepo.findById(transferRequest.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found"));
        Account toAccount = accountRepo.findById(transferRequest.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        // Perform transfer logic
        if (fromAccount.getBalance() < transferRequest.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - transferRequest.getAmount());
        toAccount.setBalance(toAccount.getBalance() + transferRequest.getAmount());

        // Save updated accounts
        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);
    }

    @Override
    public AccountDto withdraw(Long id, double amount) {

        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        if (amount > account.getBalance())
            throw new RuntimeException("insufficient funds brokie");

        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepo.save(account);

        return AccountMapper.MapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepo.findAll();
        return accounts.stream().map(AccountMapper::MapToAccountDto)
                        .collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        accountRepo.deleteById(id);
    }
}

