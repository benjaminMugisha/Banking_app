package com.benjamin.Banking_app.Accounts;

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
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest // spring to know repo tests are here.
//DataJpaTest does not mock dependencies. so verify() wont work here
//MockBean are only for service tests not repositories
//starts up an embedded database and configures Spring Data JPA. real database interactions in this integration test

// testing a custom query in LoanRepositoryTest    =>    LoanRepository_FindByAccountId_ReturnListOfLoans()
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) //h2 in memory db
public class AccountRepositoryTests {

    @Autowired //since we're testing we don't worry about constructor too much.
    private AccountRepository accountRepo;

    @Autowired
    private AccountService accountService;

    private Account account;
    private Account account1;
    @BeforeEach
    void setUp() {
        account = Account.builder() //id should be set automatically by JPA. we dont define id in repos since we're working
                //directly with the db.
                .accountUsername("John").balance(100.0)
                .build();
        account1 = Account.builder()
                .accountUsername("Peter").balance(500.0)
                .build();
    }

    @Test
    public void save_SaveValidAccount_ReturnsSavedAccount(){
        Account savedAccount = accountRepo.save(account);

        //Assert
        Assertions.assertThat(savedAccount).isNotNull();
        Assertions.assertThat(savedAccount.getId()).isGreaterThan(0);
        Assertions.assertThat(savedAccount.getAccountUsername()).isEqualTo("John");
    }

    @Test
    public void getAll_GetExistingAccounts_ShouldReturnAccount() {
        accountRepo.save(account);
        accountRepo.save(account1);
        List<Account> accountList = accountRepo.findAll();

        Assertions.assertThat(accountList).isNotNull();
        Assertions.assertThat(accountList.size()).isEqualTo(2);

        assertEquals("John", accountList.get(0).getAccountUsername());
        assertEquals(100.0, accountList.get(0).getBalance());
        assertEquals("Peter", accountList.get(1).getAccountUsername());
        assertEquals(500.0, accountList.get(1).getBalance());
    }

    @Test
    public void findById_ExistingAccount_ShouldReturnAccount() {
        Account savedAccount = accountRepo.save(account);
        Optional<Account> returnedAcc = accountRepo.findById(account.getId());

        Assertions.assertThat(returnedAcc).isPresent(); //check if optional exists before calling .get()
        Account returnedAccount = returnedAcc.get();
        Assertions.assertThat(returnedAccount).isNotNull();
        Assertions.assertThat(returnedAccount.getId()).isEqualTo(savedAccount.getId());
        Assertions.assertThat(returnedAccount.getAccountUsername()).isEqualTo("John");
        Assertions.assertThat(returnedAccount.getBalance()).isEqualTo(100.0);
    }

    @Test
    public void findAll_ExistingAccounts_ShouldReturnAllAccounts() {
        accountRepo.saveAll(List.of(account, account1));

        List<Account> accounts = accountRepo.findAll();

        Assertions.assertThat(accounts).isNotEmpty();
        Assertions.assertThat(accounts.size()).isEqualTo(2);
        Assertions.assertThat(accounts.get(0).getBalance()).isEqualTo(100.0);
        Assertions.assertThat(accounts.get(1).getBalance()).isEqualTo(500.0);
    }

    @Test
    public void findAll_NoLoansExist_ShouldReturnEmptyList() {
        List<Account> accounts = accountRepo.findAll();

        Assertions.assertThat(accounts).isNotNull(); //we expect an empty list
        Assertions.assertThat(accounts).isEmpty();
    }

    @Test
    public void deleteById_ExistingAccount_ShouldRemoveAccount() {
        Account savedAccount = accountRepo.save(account);

        accountRepo.deleteById(savedAccount.getId());

        Optional<Account> deletedAccount = accountRepo.findById(account.getId());
        Assertions.assertThat(deletedAccount).isEmpty();
    }

    @Test
    public void deleteById_ExistingAccount_ShouldNotDeleteOtherAccounts() {
        Account savedAccount = accountRepo.save(account);
        accountRepo.save(account1);
        accountRepo.deleteById(savedAccount.getId());

        // Assert - Ensure only "account" is deleted
        Optional<Account> deletedAccount = accountRepo.findById(savedAccount.getId());
        List<Account> remainingLoans = accountRepo.findAll();

        Assertions.assertThat(deletedAccount).isEmpty();
        Assertions.assertThat(remainingLoans).hasSize(1);
        Assertions.assertThat(remainingLoans.get(0).getId()).isEqualTo(account1.getId());
    }

    @Test
    public void findById_ShouldReturnEmptyOptional_WhenLoanDoesNotExist() {
        Optional<Account> returnedAccount = accountRepo.findById(5L);
        Long notRealId = 5L;

        Assertions.assertThat(returnedAccount).isEmpty();
        assertThatThrownBy(() -> accountService.deleteAccount(notRealId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }
}