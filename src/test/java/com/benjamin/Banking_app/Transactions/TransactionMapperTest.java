package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionMapperTest {

    @Test
    void mapToTransactionDto_shouldReturnNull_whenTransactionIsNull() {
        TransactionDto dto = TransactionMapper.mapToTransactionDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void mapToTransactionDto_shouldMapAllFieldsCorrectly() {
        Account account = Account.builder()
                .id(1L)
                .accountUsername("john").balance(BigDecimal.valueOf(1000))
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId(99L).account(account)
                .type(TransactionType.DEPOSIT).amount(BigDecimal.valueOf(500))
                .time(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        TransactionDto dto = TransactionMapper.mapToTransactionDto(transaction);

        assertThat(dto.getTransactionId()).isEqualTo(99L);
        assertThat(dto.getAccountUsername()).isEqualTo("john");
        assertThat(dto.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(dto.getAmount()).isEqualByComparingTo("500");
        assertThat(dto.getTimestamp()).isEqualTo(
                LocalDateTime.of(2025, 1, 1, 12, 0));
    }
}
