package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    public TransactionRepository transactionRepo;
    @InjectMocks
    public TransactionServiceImpl transactionService;
    public Account account;
    public Transaction transaction1;
    private Transaction transaction2;
    private Page<Transaction> transactionPage;

    @BeforeEach
    void setUp() {
        transaction1 = Transaction.builder().transactionId(1L).amount(10.0).type("TEST").build();
        transaction2 = Transaction.builder().transactionId(2L).amount(20.0).type("TEST").build();
        List<Transaction> transactionList = List.of(transaction1, transaction2);
        transactionPage = new PageImpl<>(transactionList);
    }


    @Test
    void findByAccountId_ExistingTransactions_ShouldReturnMappedTransactions() {
        long accountId = 1L;
        int pageNo = 0, pageSize = 2;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        when(transactionRepo.findByAccountId(accountId, pageable)).thenReturn(transactionPage);

        TransactionResponse result = transactionService.findByAccountId(accountId, pageNo, pageSize);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getAmount()).isEqualTo(transaction1.getAmount());
        assertThat(result.getContent().get(0).getType()).isEqualTo(transaction1.getType());
        assertThat(result.getContent().get(1).getAmount()).isEqualTo(transaction2.getAmount());
        assertThat(result.getContent().get(1).getType()).isEqualTo(transaction2.getType());

        verify(transactionRepo, times(1)).findByAccountId(accountId, pageable);
    }

    @Test
    void findByAccountId_nonExistingTransactions_ShouldReturnEmptyList() {
        Long accountId = 1L;
        int pageNo = 0, pageSize = 2;
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Transaction> emptyPage = Page.empty();

        when(transactionRepo.findByAccountId(accountId, pageable)).thenReturn(emptyPage);

        TransactionResponse result = transactionService.findByAccountId(accountId, pageNo, pageSize);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(transactionRepo, times(1)).findByAccountId(accountId, pageable);
    }
}
