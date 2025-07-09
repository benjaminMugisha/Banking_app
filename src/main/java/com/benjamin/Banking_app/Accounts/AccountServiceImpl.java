package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Exception.BadRequestException;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionServiceImpl transactionService;
    private final DirectDebitRepo directDebitRepo;
   private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        logger.info(" creating an account...");
        if (accountDto == null || accountDto.getAccountUsername() == null || accountDto.getBalance() <= 0) {
            throw new BadRequestException("Invalid account data");
        }
        if (accountRepository.findByAccountUsername(accountDto.getAccountUsername()).isPresent()) {
//            if (accountRepository.existsByAccountUsername(accountDto.getAccountUsername())) {
            logger.warn("attempt to create an account with a duplicate username");
            throw new BadRequestException("username already exists");
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
        authorizeAccess( account);
        logger.info(" searched account: {} returned successfuly " ,account );
        return AccountMapper.MapToAccountDto(account);
    }

    @Override
    public AccountDto deposit(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("account used not found"));

        authorizeAccess( account);
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
        authorizeAccess( fromAccount);
        if (fromAccount.getBalance() < transferRequest.getAmount()) {
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
    public AccountDto withdraw(Long id, double amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));
        authorizeAccess(account);
        if (amount > account.getBalance()){
            throw new InsufficientFundsException("insufficient funds");
        }
        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        transactionService.recordTransaction(account, "WITHDRAW", amount, "Withdraw of " + amount,
                null, null);

        return AccountMapper.MapToAccountDto(savedAccount);
    }

    //create and save the direct debit
    @Override
    public DirectDebit createDirectDebit(Long fromId, Long toId, Double amount) {
        DirectDebit dd =  DirectDebit.builder()
                .fromAccountId(fromId).toAccountId(toId).amount(amount).active(true)
                .build();
        return directDebitRepo.save(dd);
    }
    public void cancelDirectDebit(Long id){
        DirectDebit dd = directDebitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("direct debit not found"));
            dd.setActive(false); //mark it inactive so we keep the record of it instead of deleting
            directDebitRepo.save(dd);
    }

    @Scheduled(initialDelay = 2419200000L, fixedDelay = 2419200000L) //28 days in ms
    public void processDirectDebits(){
        List<DirectDebit> activeDebits = directDebitRepo.findByActiveTrue();
        for(DirectDebit dd : activeDebits){
            try {
                TransferRequest transferRequest = new TransferRequest(
                        dd.getFromAccountId(), dd.getToAccountId(), dd.getAmount());
                transfer(transferRequest);
            } catch (Exception e){
                logger.error("failed to process the direct debit. from: {}, to: {}, Amount: {}" ,
                        dd.getFromAccountId(), dd.getToAccountId(),  dd.getAmount());
                throw new RuntimeException("failed to process the direct debit");
            }
        }
    }

    @Override
    public AccountResponse getAllAccounts(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Account> accounts = accountRepository.findAll(pageable);
        List<AccountDto> content = accounts.stream().map(AccountMapper::MapToAccountDto)
                .collect(Collectors.toList());
        return AccountResponse.builder()
                .content(content).pageNo(accounts.getNumber()).pageSize(accounts.getSize())
                .totalElements(accounts.getTotalElements()).totalPages(accounts.getTotalPages())
                .last(accounts.isLast()).build();
    }
    @Override
    public void deleteAccount(Long id) {
        accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));

        accountRepository.deleteById(id);
        logger.info("account: {} deleted successfully", id);
    }
    public void authorizeAccess(Account account){
        String authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!account.getUser().getEmail().equals(authenticatedUser)){
            throw new AccessDeniedException("not the owner of the account");
        }
    }
}
