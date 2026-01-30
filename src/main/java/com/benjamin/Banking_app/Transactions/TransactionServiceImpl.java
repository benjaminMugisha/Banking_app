package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.UserUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserUtils userUtils;

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    public TransactionResponse transactions(
            int pageNo, int pageSize, String accountUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Account accountToQuery;

        if(isAdmin && accountUsername != null) {
            accountToQuery = accountRepository.findByAccountUsername(accountUsername)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountUsername));
        } else {
            accountToQuery = userUtils.getCurrentUserAccount();
        }

        long accountId = accountToQuery.getId();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        List<TransactionDto> transactionDtos = transactions.getContent().stream().map(TransactionMapper::mapToTransactionDto)
                .collect(Collectors.toList());
        TransactionResponse transactionResponse = TransactionResponse.builder().content(transactionDtos)
                .pageNo(transactions.getNumber()).pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements()).totalPages(transactions.getTotalPages())
                .last(transactions.isLast())
                .build();
        logger.info("Returned {} transactions history for account ID: {}", transactionDtos.size(), accountId);
        return transactionResponse;
    }

    public void recordTransaction(Account account, TransactionType type,
                                  BigDecimal amount,
                                  Account toAccount) {

        Transaction transaction = Transaction.builder()
                .account(account).type(type)
                .amount(amount)
                .time(OffsetDateTime.now())
                .toAccount(toAccount)
                .build();

        try{
            transactionRepository.save(transaction);
            logger.info("Transaction saved successfully. AccountId: {}, Type: {}, Amount: {}",
                    account.getId(), type, amount );

        } catch (Exception e) {
            logger.error("Failed to save transaction. AccountId: {}, Type: {}, Amount: {}, Error: {}",
                    account.getId(), type, amount, e.getMessage());
            throw e;
        }
    }
}
