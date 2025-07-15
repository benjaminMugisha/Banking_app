package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Exception.LoanAlreadyPaidException;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {
    @Mock
    private LoanRepository loanRepo;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionServiceImpl transactionService;
    @InjectMocks
    private LoanServiceImpl loanService;
    private Loan loan1;
    private Loan loan2;
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(1L, "John", 5000);
        loan1 = new Loan(1L, account, 10, LocalDateTime.now(),10.0, 1.0);
        loan2 = new Loan(2L, account, 12, LocalDateTime.now(),15.0, 2.0);
    }
    @Test
    void applyForLoan_NonExistingAccount_ShouldThrowException() {
        LoanRequest request = new LoanRequest(loan1.getLoanId(), 5000.0, 10000.0, 12);
        when(accountRepository.findById(request.getAccountId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.applyForLoan(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account with id: 1 not found");

        verify(loanRepo, times(0)).save(any(Loan.class));
    }
    @Test
    void getLoanByLoanId_NonExistingLoans_ShouldReturnLoan() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));

        Loan result = loanService.getLoanByLoanId(loan1.getLoanId());

        assertThat(result).isNotNull();
        assertThat(result.getLoanId()).isEqualTo(loan1.getLoanId());
        assertThat(result.getPrincipal()).isEqualTo(10.0);
        verify(loanRepo, times(1)).findById(loan1.getLoanId());
    }

    @Test
    void getLoanByLoanId_NonExistingLoans_ShouldThrowException() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.getLoanByLoanId(loan1.getLoanId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("loan with id: " + loan1.getLoanId() + " not found");

        verify(loanRepo, times(1)).findById(loan1.getLoanId());
    }
    @Test
    void getLoansByAccountId_ExistingLoans_ShouldReturnLoans() {
        List<Loan> loans = Arrays.asList(loan1, loan2);

        when(loanRepo.findByAccountId(account.getId())).thenReturn(loans);
        List<Loan> result = loanService.getLoansByAccountId(account.getId());

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(loan1, loan2);
        verify(loanRepo, times(1)).findByAccountId(account.getId());
    }
    @Test
    void getLoansByAccountId_NonExistingLoans_ShouldReturnEmptyList() {
        when(loanRepo.findByAccountId(account.getId())).thenReturn(Collections.emptyList());

        List<Loan> result = loanService.getLoansByAccountId(account.getId());

        assertThat(result).isEmpty();
        verify(loanRepo, times(1)).findByAccountId(account.getId());
    }
    @Test
    void repayLoanEarly_FullyPaidLoan_ShouldReturnSuccessMessage() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));

        LoanResponse response = loanService.repayLoanEarly(loan1.getLoanId());

        assertThat(response.getMessage()).contains("Loan of fully repaid early successfully, 2% penalty applied");
        assertThat(loan1.getRemainingBalance()).isEqualTo(0);
        verify(loanRepo, times(1)).save(loan1);
    }
    @Test
    void repayLoanEarly_InsufficientFunds_ShouldReturnError() {
        loan1.setRemainingBalance(9999999.0);

        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));
        LoanResponse response = loanService.repayLoanEarly(loan1.getLoanId());
        assertThat(response.getMessage()).isEqualTo("insufficient funds to clear the loan.");
    }
    @Test
    void deleteLoan_ExistingLoans_ShouldDeleteLoan() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));

        loanService.deleteLoan(loan1.getLoanId());

        verify(loanRepo).deleteById(loan1.getLoanId());
        verify(loanRepo, times(1)).deleteById(loan1.getLoanId());
    }
    @Test
    void deleteLoan_LoanNotFound_ShouldThrowException() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.deleteLoan(loan1.getLoanId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("loan with id: " + loan1.getLoanId() +" not found");

        verify(loanRepo, never()).deleteById(anyLong());
    }
    @Test
    void processMonthlyRepayment_SufficientFunds_ShouldRepaySuccessfully() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));

        LoanResponse response = loanService.processMonthlyRepayment(loan1.getLoanId(), loan1.getAmountToPayEachMonth());

        assertThat(response.getMessage()).contains("Loan repayment of " + loan1.getAmountToPayEachMonth() + " processed successfully. Remaining balance is: "
        + loan1.getRemainingBalance());
        assertThat(loan1.getRemainingBalance()).isEqualTo(9.0);
        verify(loanRepo, times(1)).save(loan1);
    }
    @Test
    void processMonthlyRepayment_AmountIsZeroOrNegative_ShouldUseDefaultAmount() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));

        LoanResponse response = loanService.processMonthlyRepayment(loan1.getLoanId(), -5.0);

        assertThat(response.getMessage()).contains("Loan repayment of " + loan1.getAmountToPayEachMonth() + " processed successfully. Remaining balance is: "
                + loan1.getRemainingBalance());
        assertThat(loan1.getRemainingBalance()).isEqualTo(9.0);
    }
    @Test
    void processMonthlyRepayment_PaymentEqualsToLoanBalance_ShouldUseLoanBalanceAsPaymentAmount() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(loanRepo.save(any(Loan.class))).thenReturn(loan1);

        LoanResponse response = loanService.processMonthlyRepayment(1L, 150.0);

        assertThat(loan1.getRemainingBalance()).isEqualTo(0);
        assertThat(account.getBalance()).isEqualTo(4990.0);
        assertThat(response.getMessage()).contains("Loan repayment of 10.0 processed successfully");
    }
    @Test
    void processMonthlyRepayment_LoanAlreadyFullyPaid_ShouldThrowException() {
        loan1.setRemainingBalance(0.0);
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.of(loan1));

        assertThatThrownBy(() -> loanService.processMonthlyRepayment(loan1.getLoanId(), loan1.getAmountToPayEachMonth()))
                .isInstanceOf(LoanAlreadyPaidException.class)
                .hasMessage("loan fully paid");
    }
    @Test
    void processMonthlyRepayment_InsufficientFunds_ShouldThrowException() {
        loan1.setAmountToPayEachMonth(9999999.0);

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan1));

        assertThatThrownBy(() -> loanService.processMonthlyRepayment(loan1.getLoanId(), loan1.getAmountToPayEachMonth()))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds");
    }
    @Test
    void processMonthlyRepayment_LoanNotFound_ShouldThrowException() {
        when(loanRepo.findById(loan1.getLoanId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.processMonthlyRepayment(loan1.getLoanId(), loan1.getAmountToPayEachMonth()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Loan with id: " + loan1.getLoanId() + " not found");
    }
}
