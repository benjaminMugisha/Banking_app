package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.UserUtils;
import com.benjamin.Banking_app.Transactions.TransactionService;
import com.benjamin.Banking_app.Transactions.TransactionType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final UserUtils userUtils;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public AccountPageResponse getAllAccounts(int pageNo, int pageSize) {
        Account account = userUtils.getCurrentUserAccount();
        logger.info(account.getAccountUsername() + " is retrieving all accounts ...");
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Account> accounts = accountRepository.findAll(pageable);
        List<AccountDto> content = accounts.stream().map(AccountMapper::MapToAccountDto)
                .collect(Collectors.toList());
        int totalPages = accounts.getTotalPages() == 0 ? 1 : accounts.getTotalPages();

        return AccountPageResponse.builder()
                .content(content).pageNo(accounts.getNumber()).pageSize(accounts.getSize())
                .totalElements(accounts.getTotalElements()).totalPages(totalPages)
                .last(accounts.isLast()).build();
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("account not found"));
        authorizeAccess(account);
        logger.info(" searched account: {} returned successfuly ", account);
        return AccountMapper.MapToAccountDto(account);
    }

    @Override
    @Transactional
    public AccountDto deposit(BigDecimal amount) {
        Account account = userUtils.getCurrentUserAccount();
        BigDecimal total = account.getBalance().add(amount);
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        transactionService.recordTransaction(account, TransactionType.DEPOSIT,
                amount, null);

        return AccountMapper.MapToAccountDto(savedAccount);
    }

    @Override
    @Transactional
    public AccountDto transfer(TransferRequest transferRequest) {
        Account fromAccount = userUtils.getCurrentUserAccount();
        Account toAccount = accountRepository.findByIban(
                transferRequest.getToIban())
                .orElseThrow(() -> new EntityNotFoundException(
                        "account to receive the funds not found"));

        Account updatedAccount = processTransfer(fromAccount, toAccount, transferRequest.getAmount());
        return AccountMapper.MapToAccountDto(updatedAccount);
    }

    private Account processTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionService.recordTransaction(
                fromAccount, TransactionType.TRANSFER_OUT, amount,
                toAccount
        );

        transactionService.recordTransaction(
                toAccount, TransactionType.TRANSFER_IN, amount,
                null
        );

        return fromAccount;
    }

    @Override
    @Transactional
    public AccountDto withdraw(BigDecimal amount) {
        Account account = userUtils.getCurrentUserAccount();
        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException("insufficient funds");
        }
        BigDecimal total = account.getBalance().subtract(amount);
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        transactionService.recordTransaction(account, TransactionType.WITHDRAW, amount,
                 null);

        return AccountMapper.MapToAccountDto(savedAccount);
    }

//    @Override
//    public void deleteAccount(Long id) {
//        accountRepository.deleteById(id);
//        logger.info("account: {} deleted successfully", id);
//    }

    //ensure only currently authenticated user or admin have access.
    public void authorizeAccess(Account account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUser = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = account.getUser().getEmail().equals(authenticatedUser);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("not the owner of the account");
        }
    }
}
