package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

public class LoanMapperTest {

    @Test
    void mapToDto_shouldReturnNull_whenLoanIsNull() {
        LoanDto dto = LoanMapper.mapToDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void mapToDto_shouldMapAllFieldsCorrectly() {
        Account account1 = Account.builder()
                .id(1L)
                .accountUsername("Peter")
                .balance(BigDecimal.valueOf(1000.0))
                .build();

        Loan loan = Loan.builder()
                .loanId(1L)
                .principal(BigDecimal.valueOf(5000))
                .remainingBalance(BigDecimal.valueOf(2000))
                .account(account1)
                .amountToPayEachMonth(BigDecimal.valueOf(500))
                .startDate(LocalDate.of(2025, 1, 1))
                .nextPaymentDate(LocalDate.of(2025, 2, 1))
                .active(true)
                .build();

        LoanDto dto = LoanMapper.mapToDto(loan);

        assertThat(dto.getLoanId()).isEqualTo(1L);
        assertThat(dto.getPrincipal()).isEqualByComparingTo("5000");
        assertThat(dto.getRemainingBalance()).isEqualByComparingTo("2000");
        assertThat(dto.getLoanOwner()).isEqualTo("Peter");
        assertThat(dto.getAmountToPayEachMonth()).isEqualByComparingTo("500");
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(dto.getNextPaymentDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(dto.isActive()).isTrue();
    }
}
