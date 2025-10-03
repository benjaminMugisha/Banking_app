package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Security.IbanGenerator;
import com.benjamin.Banking_app.Security.Users;
import com.benjamin.Banking_app.UserUtils;
import com.benjamin.Banking_app.Transactions.TransactionService;
import com.benjamin.Banking_app.Transactions.TransactionType;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepo;
    @Mock private TransactionService transactionService;
    @Mock private UserUtils userUtils;

    @InjectMocks private AccountServiceImpl accountService;

    private Account account;
    private AccountDto accountDto;
    private Users user;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .email("john@example.com")
                .password("Password1234")
                .build();

        account = Account.builder()
                .id(1L)
                .accountUsername("John")
                .balance(BigDecimal.valueOf(1000))
                .user(user)
                .build();
        user.setAccount(account);

        accountDto = AccountMapper.MapToAccountDto(account);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("john@example.com",
                        "pwd", "ROLE_USER"));
    }


    @Test
    void getAllAccounts_ShouldReturnPagedResponse() {
        Page<Account> page = new PageImpl<>(List.of(account), PageRequest.of(0, 1), 1);
        when(accountRepo.findAll(any(Pageable.class))).thenReturn(page);

        AccountPageResponse response = accountService.getAllAccounts(0, 1);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(accountRepo).findAll(any(Pageable.class));
    }

    @Test
    void getAccountById_Existing_ShouldReturnAccountDto() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccountById(1L);

        assertThat(result.getId()).isEqualTo(accountDto.getId());
        assertThat(result.getAccountUsername()).isEqualTo("John");

    }

    @Test
    void getAccountById_NonExisting_ShouldThrowException() {
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }

    @Test
    void getAccountById_WhenNotOwnerAndNotAdmin_ShouldThrowAccessDenied() {
        Users other = Users.builder().email("other@example.com").build();
        Account otherAcc = Account.builder().id(2L).user(other).build();
        when(accountRepo.findById(2L)).thenReturn(Optional.of(otherAcc));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("stranger@example.com", "pwd", "ROLE_USER"));

        assertThatThrownBy(() -> accountService.getAccountById(2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("not the owner of the account");
    }

    @Test
    void deposit_ShouldIncreaseBalanceAndSave() {
        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(accountRepo.save(account)).thenReturn(account);

        AccountDto result = accountService.deposit(BigDecimal.valueOf(200));

        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1200));
        verify(accountRepo).save(account);
        verify(transactionService).recordTransaction(account, TransactionType.DEPOSIT, BigDecimal.valueOf(200), null);
    }

    @Test
    void withdraw_WithSufficientFunds_ShouldDecreaseBalance() {
        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(accountRepo.save(account)).thenReturn(account);

        AccountDto result = accountService.withdraw(BigDecimal.valueOf(300));

        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(700));
        verify(accountRepo).save(account);
        verify(transactionService).recordTransaction(account, TransactionType.WITHDRAW, BigDecimal.valueOf(300), null);
    }

    @Test
    void withdraw_WithInsufficientFunds_ShouldThrowException() {
        when(userUtils.getCurrentUserAccount()).thenReturn(account);

        assertThatThrownBy(() -> accountService.withdraw(BigDecimal.valueOf(2000)))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("insufficient funds");

        verify(accountRepo, never()).save(any());
    }

    @Test
    void transfer_WithSufficientFunds_ShouldMoveMoney() {
        Account toAccount = Account.builder()
                .id(2L).accountUsername("Peter").balance(BigDecimal.valueOf(100))
                .iban(IbanGenerator.generateIban())
                .build();

        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(accountRepo.findByIban(toAccount.getIban())).thenReturn(Optional.of(toAccount));
        when(accountRepo.save(account)).thenReturn(account);
        when(accountRepo.save(toAccount)).thenReturn(toAccount);

        TransferRequest request = new TransferRequest(toAccount.getIban(), BigDecimal.valueOf(500));
        AccountDto result = accountService.transfer(request);

        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(toAccount.getBalance()).isEqualTo(BigDecimal.valueOf(600));

        verify(transactionService).recordTransaction(account, TransactionType.TRANSFER_OUT, BigDecimal.valueOf(500), toAccount);
        verify(transactionService).recordTransaction(toAccount, TransactionType.TRANSFER_IN, BigDecimal.valueOf(500), null);
    }

    @Test
    void transfer_WhenRecipientNotFound_ShouldThrowException() {
        when(userUtils.getCurrentUserAccount()).thenReturn(account);
//        when(accountRepo.findByAccountUsername("ghost")).thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest("ghost", BigDecimal.valueOf(50));

        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account to receive the funds not found");
    }

    @Test
    void transfer_WhenInsufficientFunds_ShouldThrowException() {
        Account toAccount = Account.builder()
                .id(2L).accountUsername("Peter").balance(BigDecimal.ZERO)
                .iban(IbanGenerator.generateIban())
                .build();

        when(userUtils.getCurrentUserAccount()).thenReturn(account);
        when(accountRepo.findByIban(toAccount.getIban())).thenReturn(Optional.of(toAccount));

        TransferRequest request = new TransferRequest(toAccount.getIban(), BigDecimal.valueOf(99999));

        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient balance");
    }

    @Test
    void deleteAccount_ShouldCallRepositoryDelete() {
        accountService.deleteAccount(1L);

        verify(accountRepo).deleteById(1L);
    }
}
