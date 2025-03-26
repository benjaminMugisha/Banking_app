package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionMapperTest {

    @Test
    public void mapToTransaction_ShouldMapFieldsCorrectly_ReturnTransaction() {
        TransactionDto transactionDto = new TransactionDto(1L, new Account(), "DEPOSIT", 500.0,
                LocalDateTime.now(), "Salary payment", new Account());

        Transaction transaction = TransactionMapper.MapToTransaction(transactionDto);

        assertThat(transaction).isNotNull();
        assertEquals(transactionDto.getTransactionId(), transaction.getTransactionId());
        assertEquals(transactionDto.getAccount(), transaction.getAccount());
        assertEquals(transactionDto.getType(), transaction.getType());
        assertEquals(transactionDto.getAmount(), transaction.getAmount());
        assertEquals(transactionDto.getTimestamp(), transaction.getTime());
        assertEquals(transactionDto.getDescription(), transaction.getDescription());
        assertEquals(transactionDto.getToAccount(), transaction.getToAccount());
    }

    @Test
    public void mapToTransactionDto_ShouldMapFieldsCorrectly_ReturnTransactionDto() {
        Transaction transaction = new Transaction(2L, new Account(), "WITHDRAWAL", 200.0,
                LocalDateTime.now(), "Cash withdrawal", new Account());

        TransactionDto transactionDto = TransactionMapper.MapToTransactionDto(transaction);

        assertThat(transactionDto).isNotNull();
        assertEquals(transaction.getTransactionId(), transactionDto.getTransactionId());
        assertEquals(transaction.getAccount(), transactionDto.getAccount());
        assertEquals(transaction.getType(), transactionDto.getType());
        assertEquals(transaction.getAmount(), transactionDto.getAmount());
        assertEquals(transaction.getTime(), transactionDto.getTimestamp());
        assertEquals(transaction.getDescription(), transactionDto.getDescription());
        assertEquals(transaction.getToAccount(), transactionDto.getToAccount());
    }

    @Test
    public void mapToTransaction_ShouldHandleNullTransactionDto_ReturnNull() {
        Transaction transaction = TransactionMapper.MapToTransaction(null);
        assertThat(transaction).isNull();
    }

    @Test
    public void mapToTransactionDto_ShouldHandleNullTransaction_ReturnNull() {
        TransactionDto transactionDto = TransactionMapper.MapToTransactionDto(null);
        assertThat(transactionDto).isNull();
    }
}
