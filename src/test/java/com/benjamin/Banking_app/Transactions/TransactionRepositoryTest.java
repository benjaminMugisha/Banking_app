package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByAccountId_shouldReturnTransactionsForThatAccount() {
        Account account = Account.builder()
                .accountUsername("john").balance(BigDecimal.valueOf(1000))
                .build();
        accountRepository.save(account);

        Transaction tx1 = Transaction.builder()
                .account(account).type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(200)).time(LocalDateTime.now())
                .build();

        Transaction tx2 = Transaction.builder()
                .account(account).type(TransactionType.WITHDRAW)
                .amount(BigDecimal.valueOf(50)).time(LocalDateTime.now())
                .build();

        transactionRepository.save(tx1);
        transactionRepository.save(tx2);

        Page<Transaction> page = transactionRepository.findByAccountId(account.getId(),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getAccount()
                .getAccountUsername()).isEqualTo("john");
    }
    @Test
    void findByAccountId_shouldReturnEmptyPage_whenNoTransactions() {
        Page<Transaction> page = transactionRepository.
                findByAccountId(999L, PageRequest.of(0, 10));
        assertThat(page).isEmpty();
    }
}
