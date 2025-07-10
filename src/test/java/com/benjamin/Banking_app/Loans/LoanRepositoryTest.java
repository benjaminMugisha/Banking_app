package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class LoanRepositoryTest {
    @Autowired
    private LoanRepository loanRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private LoanService loanService;

    private Loan loan;
    private Loan loan2;
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountUsername("John").balance(5)
                .build();
        accountRepo.save(account);

       loan = Loan.builder()
                .account(account)
                .principal(50.0)
                .build();
       loan2 = Loan.builder().
               account(account).principal(30.0)
               .build();
    }
    @Test
    void FindByAccountId_ExistingAccount_ReturnListOfLoans(){
        loanRepo.saveAll(List.of(loan, loan2));

        List<Loan> loans = loanRepo.findByAccountId(account.getId());

        Assertions.assertThat(loans).isNotEmpty();
        Assertions.assertThat(loans).isNotNull();
        Assertions.assertThat(loans.size()).isEqualTo(2);
        Assertions.assertThat(loans.get(0).getAccount()).isEqualTo(account);
        Assertions.assertThat(loans.get(1).getPrincipal()).isEqualTo(30.0);
        Assertions.assertThat(loans.get(0).getPrincipal()).isEqualTo(loan.getPrincipal());
        Assertions.assertThat(loans.get(1).getPrincipal()).isEqualTo(loan2.getPrincipal());
    }

    @Test
    void FindByAccountId_EmptyLoans_ReturnNothing(){
        List<Loan> loans = loanRepo.findByAccountId(account.getId());

        Assertions.assertThat(loans).isNotNull();
        Assertions.assertThat(loans).isEmpty();
    }

    @Test
    void SaveLoan_ToExistingAccount_ReturnsSavedAccount(){

        Loan savedLoan = loanRepo.save(loan);

        Assertions.assertThat(savedLoan).isNotNull();
        Assertions.assertThat(savedLoan.getAccount()).isEqualTo(account);
        Assertions.assertThat(savedLoan.getPrincipal()).isEqualTo(50.0);
    }

    @Test
    void findAll_ExistingLoans_ShouldReturnAllLoans() {
        loanRepo.saveAll(List.of(loan, loan2));

        List<Loan> loans = loanRepo.findAll();

        Assertions.assertThat(loans).isNotEmpty();
        Assertions.assertThat(loans.size()).isEqualTo(2);
        Assertions.assertThat(loans.get(0).getPrincipal()).isEqualTo(50.0);
        Assertions.assertThat(loans.get(1).getPrincipal()).isEqualTo(30.0);
    }

    @Test
    void findAll_NonExistingLoans_ShouldReturnEmptyList() {
        List<Loan> loans = loanRepo.findAll();

        Assertions.assertThat(loans).isNotNull();
        Assertions.assertThat(loans).isEmpty();
    }

    @Test
    void deleteById_ExistingLoan_ShouldRemoveLoan() {
        loanRepo.save(loan);

        loanRepo.deleteById(loan.getLoanId());

        Optional<Loan> deletedLoan = loanRepo.findById(loan.getLoanId());
        Assertions.assertThat(deletedLoan).isEmpty();
    }

    @Test
    void deleteById_ExistingLoan_ShouldNotDeleteOtherLoans() {
        Loan savedLoan = loanRepo.save(loan);
        loanRepo.save(loan2);
        loanRepo.deleteById(savedLoan.getLoanId());

        // Assert - Ensure only loan1 is deleted
        Optional<Loan> deletedLoan = loanRepo.findById(savedLoan.getLoanId());
        List<Loan> remainingLoans = loanRepo.findAll();

        Assertions.assertThat(deletedLoan).isEmpty();
        Assertions.assertThat(remainingLoans).hasSize(1);
        Assertions.assertThat(remainingLoans.get(0).getLoanId()).isEqualTo(loan2.getLoanId());
    }

    @Test
    void findById_ExistingLoan_ShouldReturnLoan() {
        Loan savedLoan = loanRepo.save(loan);
        Optional<Loan> returnedLoan = loanRepo.findById(savedLoan.getLoanId());

        Assertions.assertThat(returnedLoan).isPresent();
        Assertions.assertThat(returnedLoan.get().getLoanId()).isEqualTo(savedLoan.getLoanId());
        Assertions.assertThat(returnedLoan.get().getPrincipal()).isEqualTo(50.0);
    }

    @Test
    void findById_NonExistingLoan_ShouldReturnEmptyOptional() {
        Optional<Loan> returnedLoan = loanRepo.findById(5L);
        long loanId = 5L;

        Assertions.assertThat(returnedLoan).isEmpty();
        assertThatThrownBy(() -> loanService.getLoanByLoanId(loanId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("loan with id: " + loanId + " not found");
    }
}
