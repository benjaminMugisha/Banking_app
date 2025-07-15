package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.BadRequestException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Security.Users;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepo;
    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private DirectDebitRepo directDebitRepo;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AccountServiceImpl accountService;
    private Account account;
    private AccountDto accountDto;
    private Users user;

    @BeforeEach
    void setUp() {
        accountDto = AccountDto.builder()
                .id(1L).accountUsername("John").balance(50000.0).build();
        account = Account.builder()
                .id(1L).accountUsername("John").balance(50000.0)
                .build();
        user = Users.builder()
                .password("password").email("John@gmail.com")
                .build();
        account.setUser(user);
    }

    private void mockAuthenticatedUser(String email) {
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    @Test
    void getAllAccounts_ExistingAccountDtos_ShouldReturnAccountsDtos(){
        Account account2 = new Account(2L, "Peter", 200.0);
        List<Account> accountList = List.of(account, account2);

        Page<Account> accountPage = new PageImpl<>(accountList, PageRequest.of(0, 2), accountList.size());

        when(accountRepo.findAll(any(Pageable.class))).thenReturn(accountPage);
        AccountResponse result = accountService.getAllAccounts(0, 2);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertEquals(0, result.getPageNo());
        assertEquals(2, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void getAllAccounts_NonExistingAccounts_ShouldReturnEmptyPagedResponse() {
        Page<Account> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 2), 0);

        when(accountRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

        AccountResponse result = accountService.getAllAccounts(0, 2);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void createAccount_ValidAccountDto_ShouldReturnSavedAccountDto() {
        Account mappedAccount = AccountMapper.MapToAccount(accountDto);

        when(accountRepo.save(any(Account.class))).thenReturn(account);
        AccountDto result = accountService.createAccount(accountDto);
        assertThat(result).isNotNull();
        assertEquals(mappedAccount.getAccountUsername(), account.getAccountUsername());
        assertEquals(mappedAccount.getBalance(), account.getBalance());
        assertThat(result.getAccountUsername()).isEqualTo("John");
        assertThat(result.getBalance()).isEqualTo(50000.0);

        verify(accountRepo, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_WhenAccountDtoIsInvalid_ShouldThrowException() {

        AccountDto invalidAccountDto = new AccountDto(null, null, -100.0);

        assertThatThrownBy(() -> accountService.createAccount(invalidAccountDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid account data");
    }

    @Test
    void createAccount_WhenSecondUserHasDuplicateUsername_ShouldThrowException() {
        when(accountRepo.findByAccountUsername("John"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(account));

        when(accountRepo.save(any(Account.class))).thenReturn(account);

        accountService.createAccount(accountDto);

        AccountDto duplicateUser = AccountDto.builder()
                .id(2L).accountUsername("John").balance(30000.0).build();

        assertThatThrownBy(() -> accountService.createAccount(duplicateUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("username already exists");

        verify(accountRepo, times(1)).save(any(Account.class));
    }

    @Test
    void getAccountById_ExistingAccountDto_ShouldReturnAccountDto() {
        mockAuthenticatedUser("John@gmail.com");
        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccountById(account.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(accountDto.getId());
        assertThat(result.getAccountUsername()).isEqualTo("John");
        assertThat(result.getBalance()).isEqualTo(accountDto.getBalance());

        verify(accountRepo, times(1)).findById(account.getId());
    }

    @Test
    void getAccountById_NonExistingAccount_ShouldThrowException() {
        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> accountService.getAccountById(account.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }
    @Test
    void getAccountById_WhenUserNotOwner_ShouldThrowAccessDenied() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@gmail.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Users otherUser = new Users();
        otherUser.setEmail("user2@gmail.com");

        Account account = new Account();
        account.setId(1L);
        account.setUser(otherUser);

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.getAccountById(account.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("not the owner of the account");
    }

    @Test
    void deposit_ExistingAccount_ShouldUpdateBalance() {
        long accountId = account.getId();
        double depositAmount = 500.0;
        double expectedBalance = account.getBalance() + depositAmount;

        Account updatedAccount = new Account(accountId, "John", expectedBalance);
        accountDto.setBalance(expectedBalance);

        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepo.save(account)).thenReturn(updatedAccount);

        AccountDto result = accountService.deposit(accountId, depositAmount);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(accountDto.getId());
        assertThat(result.getAccountUsername()).isEqualTo(accountDto.getAccountUsername());
        assertThat(result.getBalance()).isEqualTo(accountDto.getBalance());
        assertThat(account.getBalance()).isEqualTo(expectedBalance);

        verify(accountRepo, times(1)).findById(accountId);
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void deposit_NonExistingAccount_ShouldThrowException() {
        double depositAmount = 500.0;

        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deposit(account.getId(), depositAmount))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account used not found");

        verify(accountRepo, times(1)).findById(account.getId());
        verify(accountRepo, times(0)).save(any(Account.class));
    }

    @Test
    void transfer_ExistingAccountAndBalanceIsSufficient_ShouldTransferAmount() {
        mockAuthenticatedUser("John@gmail.com");
        Long toAccountId = 2L;
        double transferAmount = 500.0;

        Account toAccount = new Account(toAccountId, "toUser", 200.0);

        TransferRequest transferRequest = new TransferRequest(account.getId(), toAccountId, transferAmount);

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepo.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        accountService.transfer(transferRequest);

        assertThat(account.getBalance()).isEqualTo(50000.0 - transferAmount);
        assertThat(toAccount.getBalance()).isEqualTo(200.0 + transferAmount);

        verify(accountRepo, times(1)).save(account);
        verify(accountRepo, times(1)).save(toAccount);
    }

    @Test
    void transfer_NonExistingAccount_ShouldThrowException() {
        Long toAccountId = 2L;
        double transferAmount = 500.0;

        TransferRequest transferRequest = new TransferRequest(account.getId(), toAccountId, transferAmount);

        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.transfer(transferRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account to send the funds not found");

        verify(accountRepo, times(0)).save(any(Account.class));
    }

    @Test
    void transfer_NotFoundAccount_ShouldThrowException() {
        Long toAccountId = 2L;
        double transferAmount = 500.0;

        TransferRequest transferRequest = new TransferRequest(account.getId(), toAccountId, transferAmount);

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepo.findById(toAccountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.transfer(transferRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account to receive the funds not found");

        verify(accountRepo, times(0)).save(any(Account.class));
    }

    @Test
    void transfer_InsufficientBalance_ShouldThrowException() {
        mockAuthenticatedUser("John@gmail.com");
        long toAccountId = 2L;
        double transferAmount = 99999999.9;

        Account toAccount = new Account(toAccountId, "toUser", 200.0);

        TransferRequest transferRequest = new TransferRequest(account.getId(), toAccountId, transferAmount);

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepo.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        assertThatThrownBy(() -> accountService.transfer(transferRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient balance");

        verify(accountRepo, times(0)).save(any(Account.class));
    }
    @Test
    void transfer_WhenUserIsNotAccountOwner_ShouldThrowAccessDeniedException() {
        Long toAccountId = 2L;
        TransferRequest transferRequest = new TransferRequest(account.getId(), toAccountId, 100.0);

        mockAuthenticatedUser("fake@gmail.com"); //mocking a fake user.

        Users accountOwner = Users.builder().email("owner@gmail.com").build();

        Account fromAccount = Account.builder()
                .id(account.getId()).user(accountOwner).balance(500.0).build();
        Account toAccount =  Account.builder().id(toAccountId).build();

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepo.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            accountService.transfer(transferRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("only the account owner can perform this action");
        verify(accountRepo, never()).save(any());
    }

    @Test
    void withdraw_AccountExistsAndFundsAreSufficient_ShouldReduceBalance() {
        mockAuthenticatedUser("John@gmail.com");
        double withdrawalAmount = 500.0;
        double expectedBalance = account.getBalance() - withdrawalAmount;

        Account updatedAccount = new Account(account.getId(), "John", expectedBalance);

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepo.save(account)).thenReturn(updatedAccount);

        AccountDto result = accountService.withdraw(account.getId(), withdrawalAmount);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(account.getId());
        assertThat(result.getAccountUsername()).isEqualTo(account.getAccountUsername());
        assertThat(result.getBalance()).isEqualTo(expectedBalance);
        assertThat(account.getBalance()).isEqualTo(expectedBalance);
        verify(accountRepo, times(1)).findById(account.getId());
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void withdraw_InsufficientFunds_ShouldThrowException() {
        mockAuthenticatedUser("John@gmail.com");
        double withdrawalAmount = 9999999999.0;

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(account.getId(), withdrawalAmount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("insufficient funds");

        verify(accountRepo, times(0)).save(any(Account.class));
    }

    @Test
    void withdraw_AccountNotFound_ShouldThrowException() {
        double withdrawalAmount = 500.0;
        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.withdraw(account.getId(), withdrawalAmount))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }

    @Test
    void deleteAccount_AccountExists_ShouldDeleteAccount() {
        Long accountId = 1L;
        Account account = new Account(accountId, "user123", 1000.0);

        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));

        accountService.deleteAccount(accountId);

        verify(accountRepo, times(1)).findById(accountId);
        verify(accountRepo, times(1)).deleteById(accountId);
    }
    @Test
    void deleteAccount_NonExistingAccount_ShouldThrowException() {

        assertThatThrownBy(()-> accountService.deleteAccount(account.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }

    @Test
    void createDirectDebit_ShouldSaveAndReturnDirectDebit() {
        Long fromId = 1L;
        Long toId = 2L;
        Double amount = 100.0;

        DirectDebit mockDD = DirectDebit.builder()
                .fromAccountId(fromId).toAccountId(toId).amount(amount).active(true)
                .build();

        when(directDebitRepo.save(any(DirectDebit.class))).thenReturn(mockDD);

        DirectDebit result = accountService.createDirectDebit(fromId, toId, amount);

        assertThat(result).isNotNull();
        assertThat(result.getFromAccountId()).isEqualTo(fromId);
        assertThat(result.getToAccountId()).isEqualTo(toId);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void cancelDirectDebit_NonExistingId_ShouldThrowException() {
        Long id = 42L;
        when(directDebitRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.cancelDirectDebit(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("direct debit not found");
    }

    @Test
    void createDirectDebit_ValidInputs_ShouldSaveAndReturnDirectDebit() {
        Long fromId = 1L;
        Long toId = 2L;
        Double amount = 100.0;

        DirectDebit savedDebit = DirectDebit.builder()
                .id(1L).fromAccountId(fromId).toAccountId(toId).amount(amount).active(true)
                .build();

        when(directDebitRepo.save(any(DirectDebit.class))).thenReturn(savedDebit);

        DirectDebit result = accountService.createDirectDebit(fromId, toId, amount);

        assertThat(result).isNotNull();
        assertThat(result.getFromAccountId()).isEqualTo(fromId);
        assertThat(result.getToAccountId()).isEqualTo(toId);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.isActive()).isTrue();

        verify(directDebitRepo, times(1)).save(any(DirectDebit.class));
    }

    @Test
    void cancelDirectDebit_ExistingDebit_ShouldSetActiveToFalse() {
        Long debitId = 1L;
        DirectDebit existingDebit = DirectDebit.builder()
                .id(debitId).active(true)
                .build();

        when(directDebitRepo.findById(debitId)).thenReturn(Optional.of(existingDebit));

        accountService.cancelDirectDebit(debitId);

        assertThat(existingDebit.isActive()).isFalse();
        verify(directDebitRepo).save(existingDebit);
    }

    @Test
    public void cancelDirectDebit_NonExistingDebit_ShouldThrowException() {
        Long debitId = 1L;

        when(directDebitRepo.findById(debitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.cancelDirectDebit(debitId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("direct debit not found");

        verify(directDebitRepo, never()).save(any());
    }

    @Test
    public void processDirectDebits_TransferFails_ShouldLogAndThrowRuntimeException() {
        DirectDebit dd = DirectDebit.builder()
                .id(1L).fromAccountId(1L).toAccountId(2L).amount(999999999.0).active(true)
                .build();
        when(directDebitRepo.findByActiveTrue()).thenReturn(List.of(dd));
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(new Account(2L, "toUser", 100.0)));

        assertThatThrownBy(() -> accountService.processDirectDebits())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("failed to process the direct debit");
    }
}
