package com.benjamin.Banking_app.AccountMapperTest;

import static org.assertj.core.api.Assertions.*;

import com.benjamin.Banking_app.Dto.AccountDto;
import com.benjamin.Banking_app.Entity.Account;
import com.benjamin.Banking_app.Mapper.AccountMapper;
import org.junit.jupiter.api.Test;

    public class AccountMapperTest {

        @Test
        public void mapToAccount_ShouldMapFieldsCorrectly() {
            AccountDto accountDto = new AccountDto(1L, "user123", 1000.0);

            Account account = AccountMapper.MapToAccount(accountDto);

            assertThat(account).isNotNull();
            assertThat(account.getId()).isEqualTo(1L);
            assertThat(account.getAccountUsername()).isEqualTo("user123");
            assertThat(account.getBalance()).isEqualTo(1000.0);
        }

        @Test
        public void mapToAccountDto_ShouldMapFieldsCorrectly() {
            Account account = new Account(1L, "user123", 1000.0);

            AccountDto accountDto = AccountMapper.MapToAccountDto(account);

            assertThat(accountDto).isNotNull();
            assertThat(accountDto.getId()).isEqualTo(1L);
            assertThat(accountDto.getAccountUsername()).isEqualTo("user123");
            assertThat(accountDto.getBalance()).isEqualTo(1000.0);
        }

        @Test
        public void mapToAccount_ShouldHandleNullAccountDto() {
            Account account = AccountMapper.MapToAccount(null);

            assertThat(account).isNull();
        }

        @Test
        public void mapToAccountDto_ShouldHandleNullAccount() {
            AccountDto accountDto = AccountMapper.MapToAccountDto(null);

            assertThat(accountDto).isNull();
        }
    }

