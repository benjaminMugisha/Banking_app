package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class TransactionRepositoryTest {
    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    private Account account;
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountUsername("John").balance(5)
                .build();
        accountRepo.save(account);
        transaction1 = Transaction.builder()
                .account(account).amount(50.0).time(LocalDateTime.now()).description("T1")
                .toAccount(null)
                .build();
        transaction2 = Transaction.builder()
                .account(account).amount(100.0).time(LocalDateTime.now()).description("T2")
                .toAccount(null)
                .build();
    }

    @Test
    void SaveTransaction_ValidTransaction_ReturnsSavedTransaction() {
        Transaction savedTransaction = transactionRepo.save(transaction1);

        Assertions.assertThat(savedTransaction).isNotNull();
        Assertions.assertThat(savedTransaction.getAccount()).isEqualTo(account);
        Assertions.assertThat(savedTransaction.getAmount()).isEqualTo(transaction1.getAmount());
    }

    @Test
    void FindByAccountId_ExistingTransactions_ReturnPagedListOfTransactions() {
        int pageNo = 0, pageSize = 2;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        transactionRepo.saveAll(List.of(transaction1, transaction2));

        Page<Transaction> transactionsPage = transactionRepo.findByAccountId(account.getId(), pageable);

        Assertions.assertThat(transactionsPage).isNotNull();
        Assertions.assertThat(transactionsPage.getContent()).isNotEmpty();
        Assertions.assertThat(transactionsPage.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(transactionsPage.getContent().size()).isEqualTo(2);
        Assertions.assertThat(transactionsPage.getContent().get(0).getAccount()).isEqualTo(account);
        Assertions.assertThat(transactionsPage.getContent().get(1).getAccount()).isEqualTo(account);
        Assertions.assertThat(transactionsPage.getContent().get(1).getAmount()).isEqualTo(transaction2.getAmount());
        Assertions.assertThat(transactionsPage.getContent().get(0).getAmount()).isEqualTo(transaction1.getAmount());
    }

    @Test
    void FindByAccountId_EmptyTransactions_ReturnEmptyPage() {
        int pageNo = 0, pageSize = 2;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<Transaction> transactionsPage = transactionRepo.findByAccountId(account.getId(), pageable);

        Assertions.assertThat(transactionsPage).isNotNull();
        Assertions.assertThat(transactionsPage.getContent()).isEmpty();
        Assertions.assertThat(transactionsPage.getTotalElements()).isEqualTo(0);
    }
}
