package com.benjamin.Banking_app.Service.impl;


import com.benjamin.Banking_app.Dto.AccountDto;
import com.benjamin.Banking_app.Dto.TransferRequest;
import com.benjamin.Banking_app.Entity.Account;
import com.benjamin.Banking_app.Entity.Transaction;
import com.benjamin.Banking_app.Mapper.AccountMapper;
import com.benjamin.Banking_app.Repository.AccountRepo;
import com.benjamin.Banking_app.Repository.TransactionRepository;
import com.benjamin.Banking_app.Service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepo accountRepo, TransactionRepository transactionRepository) {

        this.accountRepo = accountRepo;
        this.transactionRepository = transactionRepository;
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

        double total = account.getBalance() + amount;
        account.setBalance(total);
        Account savedAccount = accountRepo.save(account);

        // Save transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription("Deposit of " + amount);

        transactionRepository.save(transaction);

        return AccountMapper.MapToAccountDto(savedAccount);

    }
    @Transactional
    public void transfer(TransferRequest transferRequest) {

        Account fromAccount = accountRepo.findById(transferRequest.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found"));
        Account toAccount = accountRepo.findById(transferRequest.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        if (fromAccount.getBalance() < transferRequest.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - transferRequest.getAmount());
        toAccount.setBalance(toAccount.getBalance() + transferRequest.getAmount());

        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);

        Transaction outgoingTransaction = new Transaction();
        outgoingTransaction.setAccount(fromAccount);
        outgoingTransaction.setType("TRANSFER_OUT");
        outgoingTransaction.setAmount(transferRequest.getAmount());
        outgoingTransaction.setTimestamp(LocalDateTime.now());
        outgoingTransaction.setToAccount(toAccount); // Optional: set for tracking destination
        outgoingTransaction.setDescription("Transfer to account " + transferRequest.getToAccountId());
        transactionRepository.save(outgoingTransaction);

        Transaction incomingTransaction = new Transaction();
        incomingTransaction.setAccount(toAccount);
        incomingTransaction.setType("TRANSFER_IN");
        incomingTransaction.setAmount(transferRequest.getAmount());
        incomingTransaction.setTimestamp(LocalDateTime.now());
        incomingTransaction.setDescription("Received transfer from account " + transferRequest.getFromAccountId());
        transactionRepository.save(incomingTransaction);
    }
    @Override
    public AccountDto withdraw(Long id, double amount) {

        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        if (amount > account.getBalance())
            throw new RuntimeException("insufficient funds");

        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepo.save(account);

        // Save transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType("WITHDRAWAL");
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription("withdraw the amount of " + amount);

        transactionRepository.save(transaction);

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

