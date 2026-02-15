package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.Users;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionMapperTest {

    @Test
    void mapToTransactionDto_shouldReturnNull_whenTransactionIsNull() {
        TransactionDto dto = TransactionMapper.mapToTransactionDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void mapToTransactionDto_shouldMapAllFieldsCorrectly() {
        Users user1 = Users.builder()
                .firstName("user")
                .lastName("one")
                .email("userone@gmail.com")
//                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();
        Users user2 = Users.builder()
                .firstName("user")
                .lastName("two")
                .email("usertwo@gmail.com")
//                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();
        Account fromAccount = Account.builder()
                .id(1L).user(user2)
//                .accountUsername("john")
                .balance(BigDecimal.valueOf(1000))
                .build();
        Account toAccount = Account.builder()
                .id(2L).user(user2)
//                .accountUsername("john")
                .balance(BigDecimal.valueOf(5000))
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId(99L).account(fromAccount).toAccount(toAccount)
                .type(TransactionType.DEPOSIT).amount(BigDecimal.valueOf(500))
                .build();

        TransactionDto dto = TransactionMapper.mapToTransactionDto(transaction);

        assertThat(dto.getTransactionId()).isEqualTo(99L);
//        assertThat(dto.getAccountUsername()).isEqualTo("john");
        assertThat(dto.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(dto.getAmount()).isEqualByComparingTo("500");
    }
}
