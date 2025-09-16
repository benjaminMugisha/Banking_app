package com.benjamin.Banking_app.Accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccountRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByAccountUsername_ShouldReturnAccount_WhenUsernameExists() {
        Account account = Account.builder()
                .accountUsername("John")
                .balance(BigDecimal.valueOf(100.0))
                .build();
        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByAccountUsername("John");

        assertThat(result).isPresent();
        assertThat(result.get().getAccountUsername()).isEqualTo("John");
        assertThat(result.get().getBalance()).isEqualTo(BigDecimal.valueOf(100.0));
    }

    @Test
    void findByAccountUsername_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        Optional<Account> result = accountRepository.findByAccountUsername("DoesNotExist");

        assertThat(result).isNotPresent();
    }
}

