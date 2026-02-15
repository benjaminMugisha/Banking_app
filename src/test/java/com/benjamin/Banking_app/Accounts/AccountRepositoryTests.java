package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Security.IbanGenerator;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccountRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByAccountUsername_ShouldReturnAccount_WhenUsernameExists() {
        Users user1 = Users.builder()
                .firstName("Test")
                .lastName("User")
                .email("useremail@gmail.com")
                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();
        userRepository.save(user1);
        Account account = Account.builder()
//                .accountUsername("John")
                .user(user1)
                .iban(IbanGenerator.generateIban())
                .balance(BigDecimal.valueOf(100.0))
                .build();

        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByUserEmail(user1.getEmail());
//                .findByAccountUsername("John");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail()).isEqualTo("useremail@gmail.com");
        assertThat(result.get().getBalance()).isEqualTo(BigDecimal.valueOf(100.0));
    }

    @Test
    void findByAccountUsername_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        Optional<Account> result = accountRepository.findByUserEmail("DoesNotExist");

        assertThat(result).isNotPresent();
    }
}

