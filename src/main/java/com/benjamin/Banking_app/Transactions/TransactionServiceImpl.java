package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl {
    private final TransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    public List<Transaction> findByAccountId(Long accountId) {
        logger.info("transaction history of: {} returned successfully", accountId);
        return transactionRepository.findByAccountId(accountId);
    }

    public void recordTransaction(Account account, String type, double amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription(description);
        try{
            transactionRepository.save(transaction);
        logger.info("Transaction saved successfully. AccountId: {}, Type: {}, Amount: {}, Description: {}",
                account.getId(), type, String.format("%.2f", amount), description);
        } catch (Exception e) {
        logger.error("Failed to save transaction. AccountId: {}, Type: {}, Amount: {}, Description: {}, Error: {}",
                account.getId(), type, String.format("%.2f", amount), description, e.getMessage());
        }
    }

}
