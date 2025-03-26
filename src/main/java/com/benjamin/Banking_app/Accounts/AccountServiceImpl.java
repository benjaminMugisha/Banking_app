package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionServiceImpl transactionService;
   private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        logger.info(" creating an account...");

        if (accountDto == null || accountDto.getAccountUsername() == null || accountDto.getBalance() <= 0) {
            throw new IllegalArgumentException("Invalid account data");
        }
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
        account.setBalance(total); //updating the account's balance
        Account savedAccount = accountRepository.save(account); //persist it so it updates the balance

        transactionService.recordTransaction(account, "DEPOSIT", amount, "Deposit of " + amount, null, null);
        logger.info("Deposit successful. AccountId: {}, Amount: {}, New Balance: {}", accountId, amount, account.getBalance());

        return AccountMapper.MapToAccountDto(savedAccount);//using savedAccount since it's the upto date version
         }
    @Transactional
    public void transfer(TransferRequest transferRequest) {

        Account fromAccount = accountRepository.findById(transferRequest.getFromAccountId())
                .orElseThrow(() -> new EntityNotFoundException("account to send the funds not found"));
        Account toAccount = accountRepository.findById(transferRequest.getToAccountId())
                .orElseThrow(() -> new EntityNotFoundException("account to receive the funds not found"));
        logger.info("attempting to transfer Amount:{} from:{}, to: {} ",
                transferRequest.getAmount(), fromAccount.getAccountUsername(), toAccount.getAccountUsername());

        if (fromAccount.getBalance() < transferRequest.getAmount()) {
            logger.error(":{} has insufficient balance", fromAccount.getAccountUsername());
            throw new InsufficientFundsException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance() - transferRequest.getAmount());
        toAccount.setBalance(toAccount.getBalance() + transferRequest.getAmount());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionService.recordTransaction(fromAccount,
                "TRANSFER_OUT",
                transferRequest.getAmount(),
                "Transfer to " + toAccount.getAccountUsername(),
                toAccount.getAccountUsername(),
                fromAccount.getAccountUsername());

        transactionService.recordTransaction(toAccount, "TRANSFER_IN", transferRequest.getAmount(),
                "Received transfer from " + fromAccount.getAccountUsername(),
                null, fromAccount.getAccountUsername());

        logger.info("funds transferred successfully. from: {}, to: {}, Amount: {}" ,
                fromAccount.getAccountUsername(), toAccount.getAccountUsername(), transferRequest.getAmount());
    }

    @Override
    public void directDebit(TransferRequest transferRequest) {
        //schedule the existing transfer() to run every 28 days.
        logger.info("setting up a direct debit from:{} to:{}, of: {}€ every month(28 days) ",
                transferRequest.getFromAccountId(), transferRequest.getToAccountId(), transferRequest.getAmount());
        //trigger the initial transfer
        transfer(transferRequest);

        // Schedule the next payment every 28 days (28 days * 24hrs * 60 mins * 60secs * 1000ms)
        long delay = 28L * 24 * 60 * 60 * 1000;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            transfer(transferRequest); // Perform the transfer again
        }, delay, delay, TimeUnit.MILLISECONDS); // Delay, then repeat after every 28 days
    }

    @Override
    public AccountDto withdraw(Long id, double amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));

        if (amount > account.getBalance()){
            logger.error("user has insufficient funds");
            throw new InsufficientFundsException("insufficient funds");
        }
        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        transactionService.recordTransaction(account, "DEPOSIT", amount, "Deposit of " + amount,
                null, null);

        return AccountMapper.MapToAccountDto(savedAccount);
    }
    @Override
    public AccountResponse getAllAccounts(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Account> accounts = accountRepository.findAll(pageable);
        List<AccountDto> content = accounts.stream().map(AccountMapper::MapToAccountDto)
                .collect(Collectors.toList());
        AccountResponse accountResponse = AccountResponse.builder()
                .content(content).pageNo(accounts.getNumber()).pageSize(accounts.getSize())
                .totalElements(accounts.getTotalElements()).totalPages(accounts.getTotalPages())
                .last(accounts.isLast()).build();

        return accountResponse;
    }
    @Override
    public void deleteAccount(Long id) {
        accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));

        accountRepository.deleteById(id);
        logger.info("account: {} deleted successfully", id);
    }
}