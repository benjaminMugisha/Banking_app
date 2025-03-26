package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    public TransactionResponse findByAccountId(Long accountId, int pageNo, int pageSize) {
        logger.info("transaction history of: {} requested", accountId);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        List<TransactionDto> transactionDtos = transactions.getContent().stream().map(TransactionMapper::MapToTransactionDto)
                .collect(Collectors.toList());
        TransactionResponse transactionResponse = TransactionResponse.builder().content(transactionDtos)
                .pageNo(transactions.getNumber()).pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements()).totalPages(transactions.getTotalPages())
                .last(transactions.isLast())
                .build();
        logger.info("Returned {} transactions for account ID: {}", transactionDtos.size(), accountId);
        return transactionResponse;
    }

    public void recordTransaction(Account account, String type, double amount,
                                  String description, String to, String from) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTime(LocalDateTime.now());
        transaction.setDescription(description);

        try{
            Transaction savedTransaction = transactionRepository.save(transaction);
            logger.info("Transaction saved successfully. AccountId: {}, Type: {}, Amount: {}, Description: {}",
                    account.getId(), type, String.format("%.2f", amount), description);

        } catch (Exception e) {
            logger.error("Failed to save transaction. AccountId: {}, Type: {}, Amount: {}, Description: {}, Error: {}",
                    account.getId(), type, String.format("%.2f", amount), description, e.getMessage());
            throw e;
        }
    }
}