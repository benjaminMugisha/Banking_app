package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Transactions.TransactionService;
import com.benjamin.Banking_app.Transactions.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
   private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        logger.info(" creating an account...");
        Account account = AccountMapper.MapToAccount(accountDto);
        Account savedAccount = accountRepository.save(account);

        logger.info(" account: {} created successfully", account);
        return AccountMapper.MapToAccountDto(savedAccount);
    }
    @Override
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));
        logger.info(" searched account: {} returned successfuly " ,account );
        return AccountMapper.MapToAccountDto(account);
    }
    @Override
    public AccountDto deposit(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("account used not found"));

        double total = account.getBalance() + amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        transactionService.recordTransaction(account, "DEPOSIT", amount, "Deposit of " + amount);

        logger.info("Deposit successful. AccountId: {}, Amount: {}, New Balance: {}", accountId, amount, account.getBalance());

        return AccountMapper.MapToAccountDto(savedAccount);
    }
    @Transactional
    public void transfer(TransferRequest transferRequest) {
        logger.info("transfer happening...");
        Account fromAccount = accountRepository.findById(transferRequest.getFromAccountId())
                .orElseThrow(() -> new EntityNotFoundException("account to send the funds not found"));
        Account toAccount = accountRepository.findById(transferRequest.getToAccountId())
                .orElseThrow(() -> new EntityNotFoundException("account to receive the funds not found"));

        if (fromAccount.getBalance() < transferRequest.getAmount()) {
            logger.error("user sending the funds has insufficient balance");
            throw new InsufficientFundsException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - transferRequest.getAmount());
        toAccount.setBalance(toAccount.getBalance() + transferRequest.getAmount());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionService.recordTransaction(fromAccount, "TRANSFER_OUT", transferRequest.getAmount(),
                "Transfer to account " + transferRequest.getToAccountId());
        transactionService.recordTransaction(toAccount, "TRANSFER_IN", transferRequest.getAmount(),
                "Received transfer from account " + transferRequest.getFromAccountId());

        logger.info("funds transferred successfully. fromAccountId: {}, toAccountId: {}, Amount: {}" ,
                fromAccount.getId(), toAccount.getId(), transferRequest.getAmount());
    }
    @Override
    public AccountDto withdraw(Long id, double amount) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));

        if (amount > account.getBalance()){
            logger.error("user has insufficient funds");
            throw new RuntimeException("insufficient funds");
        }

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
                .orElseThrow(() -> new EntityNotFoundException("account not found"));

        accountRepository.deleteById(id);
        logger.info("account: {} deleted succesfully", id);
    }
}

