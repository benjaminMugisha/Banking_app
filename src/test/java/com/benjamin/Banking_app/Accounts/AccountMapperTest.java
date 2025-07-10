package com.benjamin.Banking_app.Accounts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountMapperTest {

    @Test
    void mapToAccount_ShouldMapFieldsCorrectly_ReturnAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountUsername("John").balance(1000.0)
                .build();
        Account account = AccountMapper.MapToAccount(accountDto);

        assertThat(account).isNotNull();
        Assertions.assertEquals(accountDto.getId(), account.getId());
        assertThat(account.getAccountUsername()).isEqualTo("John");
        Assertions.assertEquals(accountDto.getAccountUsername(), account.getAccountUsername());
        assertThat(account.getBalance()).isEqualTo(1000.0);
        Assertions.assertEquals(accountDto.getBalance(), account.getBalance());
    }

    @Test
    void mapToAccountDto_ShouldMapFieldsCorrectly_ReturnAccountDto() {
        Account account = Account.builder()
                .accountUsername("Peter").balance(50.0)
                .build();

        AccountDto accountDto = AccountMapper.MapToAccountDto(account);

        assertThat(accountDto).isNotNull();
        Assertions.assertEquals(accountDto.getId(), account.getId());
        assertThat(accountDto.getAccountUsername()).isEqualTo("Peter");
        Assertions.assertEquals(accountDto.getAccountUsername(), account.getAccountUsername());
        assertThat(accountDto.getBalance()).isEqualTo(50.0);
        Assertions.assertEquals(accountDto.getBalance(), account.getBalance());
    }

    @Test
    void mapToAccount_ShouldHandleNullAccountDto_ReturnNothing() {
        Account nullAccount = AccountMapper.MapToAccount(null);
        assertThat(nullAccount).isNull();
    }

    @Test
    void mapToAccountDto_ShouldHandleNullAccount_ReturnNothing() {
        AccountDto nullAccountDto = AccountMapper.MapToAccountDto(null);
        assertThat(nullAccountDto).isNull();
    }
}

