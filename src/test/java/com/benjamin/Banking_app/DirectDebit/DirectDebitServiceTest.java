package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Accounts.TransferRequest;
import com.benjamin.Banking_app.Security.Users;
import com.benjamin.Banking_app.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public  class DirectDebitServiceTest {

    @Mock
    private DirectDebitRepo directDebitRepo;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountServiceImpl accountService;

    @Mock
    private UserUtils userUtils;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DirectDebitServiceImpl directDebitService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fromAccount = Account.builder()
                .id(1L)
                .accountUsername("user1")
                .balance(BigDecimal.valueOf(1000.0))
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountUsername("user2")
                .balance(BigDecimal.valueOf(500.0))
                .build();

        when(userUtils.getCurrentUserAccount()).thenReturn(fromAccount);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void createDirectDebit_ShouldSaveDirectDebitAndTransferImmediately() {
        when(accountRepository.findByAccountUsername("user2")).thenReturn(Optional.of(toAccount));

        BigDecimal amount = BigDecimal.valueOf(100.0);

        DirectDebitDto dto = directDebitService.createDirectDebit("user2", amount);

        assertThat(dto).isNotNull();
        assertThat(dto.getFromAccountUsername()).isEqualTo("user1");
        assertThat(dto.getToAccountUsername()).isEqualTo("user2");
        assertThat(dto.getAmount().compareTo(amount)).isZero();
        assertThat(dto.isActive()).isTrue();

        verify(directDebitRepo).save(any(DirectDebit.class));
        verify(accountService).transfer(any(TransferRequest.class));
    }

    @Test
    void createDirectDebit_ShouldThrowIfToAccountNotFound() {
        when(accountRepository.findByAccountUsername("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                directDebitService.createDirectDebit("missing", BigDecimal.valueOf(100.0))
        );
    }

    @Test
    void cancelDirectDebit_ShouldSetActiveFalseForOwner() {
        DirectDebit debit = DirectDebit.builder()
                .id(1L)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(BigDecimal.valueOf(100))
                .active(true)
                .nextPaymentDate(LocalDate.now())
                .build();

        when(directDebitRepo.findById(1L)).thenReturn(Optional.of(debit));
        when(authentication.getAuthorities()).thenReturn(List.of()); // not admin

        directDebitService.cancelDirectDebit(1L);

        assertThat(debit.isActive()).isFalse();
        verify(directDebitRepo).save(debit);
    }

    @Test
    void cancelDirectDebit_ShouldThrowAccessDeniedForNonOwnerNonAdmin() {
        Users otherUser = new Users();
        Account otherAccount = Account.builder().id(3L).accountUsername("other").build();
        when(userUtils.getCurrentUserAccount()).thenReturn(otherAccount);

        DirectDebit debit = DirectDebit.builder()
                .id(1L)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(BigDecimal.valueOf(100))
                .active(true)
                .build();

        when(directDebitRepo.findById(1L)).thenReturn(Optional.of(debit));
        when(authentication.getAuthorities()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> directDebitService.cancelDirectDebit(1L));
    }

    @Test
    void getById_ShouldReturnDirectDebitDto() {
        DirectDebit debit = DirectDebit.builder()
                .id(1L)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(BigDecimal.valueOf(50))
                .active(true)
                .nextPaymentDate(LocalDate.now())
                .build();

        when(directDebitRepo.findById(1L)).thenReturn(Optional.of(debit));

        DirectDebitDto dto = directDebitService.getById(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void getById_ShouldThrowIfNotFound() {
        when(directDebitRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> directDebitService.getById(1L));
    }
}
