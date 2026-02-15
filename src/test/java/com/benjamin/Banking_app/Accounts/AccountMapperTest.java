package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.Users;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountMapperTest {

    @Test
    void mapToAccountDto_ShouldMapFieldsCorrectly() {
        Users user = Users.builder()
                .firstName("Test")
                .lastName("User")
                .email("testloan@mail.com")
//                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();

        Account account = Account.builder()
                .id(1L)
                .user(user)
//                .accountUsername("Peter")
                .balance(BigDecimal.valueOf(1000.0))
                .build();

        AccountDto dto = AccountMapper.MapToAccountDto(account);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
//        assertThat(dto.getAccountUsername()).isEqualTo("Peter");
        assertThat(dto.getBalance()).isEqualTo(BigDecimal.valueOf(1000.0));
    }

    @Test
    void mapToAccountDto_ShouldReturnNull_WhenAccountIsNull() {
        AccountDto dto = AccountMapper.MapToAccountDto(null);

        assertThat(dto).isNull();
    }
}
