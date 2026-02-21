package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Security.IbanGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account account;

    @BeforeEach
    void setUp() {
        account = accountRepository.save(
                Account.builder()
//                        .accountUsername("john").balance(BigDecimal.valueOf(2000))
                        .iban(IbanGenerator.generateIban())
                        .build()
        );

        Loan loan = Loan.builder()
                .account(account)
                .principal(BigDecimal.valueOf(1000))
                .amountToPayEachMonth(BigDecimal.valueOf(100))
                .remainingBalance(BigDecimal.valueOf(500))
                .startDate(LocalDateTime.now())
                .nextPaymentDate(LocalDate.now().plusDays(30))
                .active(true)
                .build();

        loanRepository.save(loan);
    }

    @Test
    void findByAccountIdAndRemainingBalanceGreaterThan_ShouldReturnActiveLoans() {
        List<Loan> loans = loanRepository
                .findByAccountIdAndRemainingBalanceGreaterThan(account.getId(), 0.0);
        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).getRemainingBalance()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void findByRemainingBalanceGreaterThanAndNextPaymentDate_ShouldReturnDueLoans() {
        List<Loan> loans = loanRepository
                .findByRemainingBalanceGreaterThanAndNextPaymentDate(0.0,
                        LocalDate.now().plusDays(30));
        assertThat(loans).hasSize(1);
    }

    @Test
    void findByAccountIdAndActiveTrue_ShouldReturnActiveLoansPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Loan> loans = loanRepository.findByAccountIdAndActiveTrue(account.getId(), pageable);

        assertThat(loans.getTotalElements()).isEqualTo(1);
        assertThat(loans.getContent().get(0).isActive()).isTrue();
    }
}
