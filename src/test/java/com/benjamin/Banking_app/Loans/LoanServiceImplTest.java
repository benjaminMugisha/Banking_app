package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.UserUtils;
import com.benjamin.Banking_app.Transactions.TransactionService;
import com.benjamin.Banking_app.Transactions.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private UserUtils userUtils;
    @InjectMocks
    private LoanServiceImpl loanService;
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = Account.builder()
                .id(1L)
                .accountUsername("john")
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void applyForLoan_shouldApproveLoan_whenEligible() {
        LoanRequest request = new LoanRequest( BigDecimal.valueOf(40000),
                BigDecimal.valueOf(2000), 12);

        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(loanRepository.findByAccountIdAndRemainingBalanceGreaterThan(anyLong(), anyDouble()))
                .thenReturn(Collections.emptyList());

        LoanResponse response = loanService.applyForLoan(request);

        assertThat(response.getMessage()).contains("Loan accepted");
        verify(loanRepository).save(any(Loan.class));
        verify(transactionService).recordTransaction(eq(account),
                eq(TransactionType.LOAN_APPLICATION), any(), isNull());
    }

    @Test
    void applyForLoan_shouldRejectLoan_whenDTITooHigh() {
        LoanRequest request = new LoanRequest( BigDecimal.valueOf(10000), BigDecimal.valueOf(50000), 12);

        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(loanRepository.findByAccountIdAndRemainingBalanceGreaterThan(anyLong(), anyDouble()))
                .thenReturn(Collections.emptyList());

        LoanResponse response = loanService.applyForLoan(request);

        assertThat(response.getMessage()).contains("Loan denied");
        verify(loanRepository, never()).save(any());
        verify(transactionService, never()).recordTransaction(any(), any(), any(), any());
    }

    @Test
    void repayLoanEarly_shouldFail_ifNotLoanOwner() {
        Loan loan = Loan.builder()
                .loanId(99L)
                .account(Account.builder().id(2L).accountUsername("other").balance(BigDecimal.TEN)
                        .build())
                .remainingBalance(BigDecimal.valueOf(100))
                .build();

        when(loanRepository.findById(99L)).thenReturn(Optional.of(loan));
        when(userUtils.getCurrentUserAccount()).thenReturn(account);

        LoanResponse response = loanService.repayLoanEarly(99L);

        assertThat(response.getMessage().contains("You can not repay someone else's loan."));
        verify(transactionService, never()).recordTransaction(any(), any(), any(), any());
    }

    @Test
    void repayLoanEarly_shouldSucceed_ifFundsSufficient() {
        Loan loan = Loan.builder()
                .loanId(1L)
                .account(account)
                .remainingBalance(BigDecimal.valueOf(500))
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(userUtils.getCurrentUserAccount()).thenReturn(account);

        LoanResponse response = loanService.repayLoanEarly(1L);

        assertThat(response.getMessage()).contains("fully repaid");
        assertThat(loan.isActive()).isFalse();
        assertThat(loan.getRemainingBalance()).isZero();

        verify(transactionService).recordTransaction(eq(account),
                eq(TransactionType.LOAN_REPAYMENT), eq(BigDecimal.valueOf(500)), isNull());
        verify(accountRepository).save(account);
        verify(loanRepository).save(loan);
    }

    @Test
    void repayLoanEarly_shouldFail_ifInsufficientFunds() {
        account.setBalance(BigDecimal.valueOf(100));
        Loan loan = Loan.builder()
                .loanId(2L).account(account)
                .remainingBalance(BigDecimal.valueOf(500))
                .build();

        when(loanRepository.findById(2L)).thenReturn(Optional.of(loan));
        when(userUtils.getCurrentUserAccount()).thenReturn(account);

        LoanResponse response = loanService.repayLoanEarly(2L);

        assertThat(response.getMessage()).contains("insufficient funds");
        verify(transactionService, never()).recordTransaction(any(), any(), any(), any());
    }

    @Test
    void processDueLoanRepayments_shouldDeductBalanceAndRecordTransaction() {
        Loan loan = Loan.builder()
                .loanId(10L)
                .account(account).remainingBalance(BigDecimal.valueOf(200))
                .amountToPayEachMonth(BigDecimal.valueOf(200)).nextPaymentDate(LocalDate.now())
                .active(true)
                .build();

        when(loanRepository.findByRemainingBalanceGreaterThanAndNextPaymentDate(anyDouble(), any()))
                .thenReturn(List.of(loan));

        loanService.processDueLoanRepayments();

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(loan.getRemainingBalance()).isZero();
        assertThat(loan.isActive()).isFalse();

        verify(transactionService).recordTransaction(eq(account), eq(TransactionType.LOAN_REPAYMENT),
                eq(BigDecimal.valueOf(200)), isNull());
        verify(accountRepository).save(account);
        verify(loanRepository).save(loan);
    }
}
