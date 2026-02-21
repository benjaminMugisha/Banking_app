package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Security.IbanGenerator;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DirectDebitRepoTest {

    @Autowired
    private DirectDebitRepo directDebitRepo;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        Users user1 = Users.builder()
                .firstName("user")
                .lastName("one")
                .email("userone@gmail.com")
                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();
        Users user2 = Users.builder()
                .firstName("user")
                .lastName("two")
                .email("usertwo@gmail.com")
                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();
        userRepository.save(user1);
        userRepository.save(user2);

       account1 = accountRepository.save(Account.builder()
                .user(user1)
                .balance(BigDecimal.valueOf(1000)).iban(IbanGenerator.generateIban())
                .build());
        account2 = accountRepository.save(Account.builder()
                .user(user2)
                .balance(BigDecimal.valueOf(500)).iban(IbanGenerator.generateIban())
                .build());

        DirectDebit dd1 = DirectDebit.builder()
                .fromAccount(account1).toAccount(account2)
                .amount(BigDecimal.valueOf(100)).active(true)
                .nextPaymentDate(LocalDate.now())
                .build();

        DirectDebit dd2 = DirectDebit.builder()
                .fromAccount(account2).toAccount(account1)
                .amount(BigDecimal.valueOf(50)).active(false)
                .nextPaymentDate(LocalDate.now())
                .build();

        directDebitRepo.save(dd1);
        directDebitRepo.save(dd2);
    }

    @Test
    void findByActiveTrueAndNextPaymentDate_ShouldReturnOnlyActiveDebitsForToday() {
        LocalDate today = LocalDate.now();
        List<DirectDebit> result = directDebitRepo.findByActiveTrueAndNextPaymentDate(today);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void findByFromAccount_ShouldReturnPagedDebits() {
        List<DirectDebit> result = directDebitRepo.findByFromAccount(account1,
                PageRequest.of(0, 10)).getContent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromAccount().getUser().getEmail()).isEqualTo("userone@gmail.com");
        assertThat(result.get(0).isActive()).isTrue();
    }
}
