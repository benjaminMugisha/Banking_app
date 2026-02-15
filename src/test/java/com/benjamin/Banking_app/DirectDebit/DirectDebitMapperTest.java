package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.Users;
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

        Account fromAccount = Account.builder().id(1L).user(user1).build();
        Account toAccount = Account.builder().id(2L).user(user2).build();

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
        assertThat(dto.getFromAccountUsername()).isEqualTo("userone@gmail.com");
        assertThat(dto.getToAccountUsername()).isEqualTo("usertwo@gmail.com");
        assertThat(dto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(dto.getNextPaymentDate()).isEqualTo(LocalDate.of(2025, 9, 15));
        assertThat(dto.isActive()).isTrue();
    }
}
