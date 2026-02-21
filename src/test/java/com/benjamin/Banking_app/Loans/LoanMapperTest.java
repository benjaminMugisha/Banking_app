package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class LoanMapperTest {

//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    @Autowired
//    private UserRepository userRepository;

    @Test
    void mapToDto_shouldReturnNull_whenLoanIsNull() {
        LoanDto dto = LoanMapper.mapToDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void mapToDto_shouldMapAllFieldsCorrectly() {
        Users user1 = Users.builder()
                .firstName("user")
                .lastName("1")
                .email("testloan@gmail.com")
                .active(true)
                .role(Role.USER)
                .build();

        Account account1 = Account.builder()
                .id(1L)
                .user(user1)
                .balance(BigDecimal.valueOf(1000.0))
                .build();

        Loan loan = Loan.builder()
                .loanId(1L)
                .principal(BigDecimal.valueOf(5000))
                .remainingBalance(BigDecimal.valueOf(2000))
                .account(account1)
                .amountToPayEachMonth(BigDecimal.valueOf(500))
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .nextPaymentDate(LocalDate.of(2025, 2, 1))
                .active(true)
                .build();

        LoanDto dto = LoanMapper.mapToDto(loan);

        assertThat(dto.getLoanId()).isEqualTo(1L);
        assertThat(dto.getPrincipal()).isEqualByComparingTo("5000");
        assertThat(dto.getRemainingBalance()).isEqualByComparingTo("2000");
        assertThat(dto.getLoanOwner()).isEqualTo("testloan@gmail.com");
        assertThat(dto.getAmountToPayEachMonth()).isEqualByComparingTo("500");
        assertThat(dto.getStartDate()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(dto.getNextPaymentDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(dto.isActive()).isTrue();
    }
}
