package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountUsername("john")
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void transactions_shouldReturnTransactionsForCurrentUser() {
        Transaction tx = Transaction.builder()
                .transactionId(10L)
                .account(account).type(TransactionType.WITHDRAW)
                .amount(BigDecimal.valueOf(100))
                .time(LocalDate.now().now())
                .build();

        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn(List.of());
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(transactionRepository.findByAccountId(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tx)));

        TransactionResponse response = transactionService
                .transactions(0, 10, null);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getType()).isEqualTo(TransactionType.WITHDRAW);
        verify(transactionRepository).findByAccountId(eq(1L), any(PageRequest.class));
    }

    @Test
    void recordTransaction_shouldSaveTransaction() {
        transactionService.recordTransaction(account,
                TransactionType.DEPOSIT, BigDecimal.valueOf(300), null);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void recordTransaction_shouldThrowException_whenSaveFails() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("DB error"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        transactionService.recordTransaction(account,
                                TransactionType.DEPOSIT, BigDecimal.valueOf(300), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}
