package com.benjamin.Banking_app.Accounts;


import com.benjamin.Banking_app.Transactions.TransactionService;
import com.benjamin.Banking_app.Transactions.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;


    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.MapToAccount(accountDto);
        Account savedAccount = accountRepository.save(account);

        return AccountMapper.MapToAccountDto(savedAccount);
    }
    @Override
    public AccountDto getAccountById(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));
        return AccountMapper.MapToAccountDto(acc);
    }
    @Override
    public AccountDto deposit(Long id, double amount) {
        Account account = accountRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("account does not exist"));

        double total = account.getBalance() + amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        // Save transaction record
        transactionService.recordTransaction(account, "DEPOSIT", amount, "Deposit of " + amount);

        return AccountMapper.MapToAccountDto(savedAccount);

    }
    @Transactional
    public void transfer(TransferRequest transferRequest) {

        Account fromAccount = accountRepository.findById(transferRequest.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("From account not found"));
        Account toAccount = accountRepository.findById(transferRequest.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        if (fromAccount.getBalance() < transferRequest.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - transferRequest.getAmount());
        toAccount.setBalance(toAccount.getBalance() + transferRequest.getAmount());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Delegate transaction recording to TransactionService
        transactionService.recordTransaction(fromAccount, "TRANSFER_OUT", transferRequest.getAmount(),
                "Transfer to account " + transferRequest.getToAccountId());
        transactionService.recordTransaction(toAccount, "TRANSFER_IN", transferRequest.getAmount(),
                "Received transfer from account " + transferRequest.getFromAccountId());
    }
    @Override
    public AccountDto withdraw(Long id, double amount) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        if (amount > account.getBalance())
            throw new RuntimeException("insufficient funds");

        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        transactionService.recordTransaction(account, "DEPOSIT", amount, "Deposit of " + amount);

        return AccountMapper.MapToAccountDto(savedAccount);
    }
    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream().map(AccountMapper::MapToAccountDto)
                        .collect(Collectors.toList());
    }
    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("account does not exist"));

        accountRepository.deleteById(id);
    }
}

