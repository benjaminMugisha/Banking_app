package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

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

    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        account1 = accountRepository.save(Account.builder().
                accountUsername("user1").balance(BigDecimal.valueOf(1000))
                .build());
        account2 = accountRepository.save(Account.builder()
                .accountUsername("user2").balance(BigDecimal.valueOf(500))
                .build());

        DirectDebit dd1 = DirectDebit.builder()
                .fromAccount(account1).toAccount(account2)
                .amount(BigDecimal.valueOf(100)).active(true)
                .nextPaymentDate(LocalDate.now())
                .build();

        DirectDebit dd2 = DirectDebit.builder()
                .fromAccount(account1).toAccount(account2)
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
    void findByFromAccountAndActiveTrue_ShouldReturnPagedDebits() {
        List<DirectDebit> result = directDebitRepo.findByFromAccountAndActiveTrue(account1,
                PageRequest.of(0, 10)).getContent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromAccount().getAccountUsername()).isEqualTo("user1");
        assertThat(result.get(0).isActive()).isTrue();
    }
}
