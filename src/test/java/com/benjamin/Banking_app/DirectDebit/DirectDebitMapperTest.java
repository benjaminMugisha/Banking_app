package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectDebitMapperTest {

    @Test
    void mapToDirectDebitDto_ShouldReturnNull_WhenInputIsNull() {
        DirectDebitDto dto = DirectDebitMapper.mapToDirectDebitDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void mapToDirectDebitDto_ShouldMapAllFields() {
        Account fromAccount = Account.builder().id(1L).accountUsername("user1").build();
        Account toAccount = Account.builder().id(2L).accountUsername("user2").build();

        DirectDebit directDebit = DirectDebit.builder()
                .id(100L)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(BigDecimal.valueOf(250))
                .nextPaymentDate(LocalDate.of(2025, 9, 15))
                .active(true)
                .build();

        DirectDebitDto dto = DirectDebitMapper.mapToDirectDebitDto(directDebit);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getFromAccountUsername()).isEqualTo("user1");
        assertThat(dto.getToAccountUsername()).isEqualTo("user2");
        assertThat(dto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(dto.getNextPaymentDate()).isEqualTo(LocalDate.of(2025, 9, 15));
        assertThat(dto.isActive()).isTrue();
    }
}
