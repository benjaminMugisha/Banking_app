package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Exception.EntityNotFoundException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepo;
    @Mock
    private TransactionServiceImpl transactionService;
    @InjectMocks
    private AccountServiceImpl accountService;
    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        accountDto = AccountDto.builder()
                .id(1L).accountUsername("John").balance(50000.0).build();
        account = Account.builder()
                .id(1L).accountUsername("John").balance(50000.0)
                .build();
    }
    @Test
    public void getAllAccounts_ExistingAccountDtos_ShouldReturnAccountsDtos(){
        Account account2 = new Account(2L, "Peter", 200.0);
        List<Account> accountList = List.of(account, account2);

        Page<Account> accountPage = new PageImpl<>(accountList, PageRequest.of(0, 2), accountList.size());

        when(accountRepo.findAll(any(Pageable.class))).thenReturn(accountPage);
        // Act
        AccountResponse result = accountService.getAllAccounts(0, 2);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertEquals(0, result.getPageNo()); // First page
        assertEquals(2, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages()); // we requested 2 per page
        assertTrue(result.isLast());
    }
    @Test
    public void getAllAccounts_NonExistingAccounts_ShouldReturnEmptyPagedResponse() {
        // Arrange
        Page<Account> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 2), 0);

        when(accountRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        AccountResponse result = accountService.getAllAccounts(0, 2);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.isLast()); // Should be last since it's empty
    }
    @Test
    public void createAccount_ValidAccountDto_ShouldReturnSavedAccountDto() {
        Account mappedAccount = AccountMapper.MapToAccount(accountDto);

        when(accountRepo.save(mappedAccount)).thenReturn(account);
        AccountDto result = accountService.createAccount(accountDto);
        assertThat(result).isNotNull();
        assertEquals(mappedAccount.getAccountUsername(), account.getAccountUsername());
        assertEquals(mappedAccount.getBalance(), account.getBalance());
        assertThat(result.getAccountUsername()).isEqualTo("John");
        assertThat(result.getBalance()).isEqualTo(50000.0);

        verify(accountRepo, times(1)).save(mappedAccount);
    }
    @Test
    public void createAccount_WhenAccountDtoIsInvalid_ShouldThrowException() {

        AccountDto invalidAccountDto = new AccountDto(0, null, -100.0);

        assertThatThrownBy(() -> accountService.createAccount(invalidAccountDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid account data");
    }
    @Test
    public void getAccountById_ExistingAccountDto_ShouldReturnAccountDto() {
        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccountById(account.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(accountDto.getId());
        assertThat(result.getAccountUsername()).isEqualTo("John");
        assertThat(result.getBalance()).isEqualTo(accountDto.getBalance());

        verify(accountRepo, times(1)).findById(account.getId());
    }
    @Test
    public void getAccountById_NonExistingAccount_ShouldThrowException() {
        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> accountService.getAccountById(account.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }
    @Test
    public void deposit_ExistingAccount_ShouldUpdateBalance() {
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
    public void deposit_NonExistingAccount_ShouldThrowException() {
        double depositAmount = 500.0;

        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deposit(account.getId(), depositAmount))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account used not found");

        verify(accountRepo, times(1)).findById(account.getId());
        verify(accountRepo, times(0)).save(any(Account.class));  // Ensure save is not called
    }
    @Test
    public void transfer_ExistingAccountAndBalanceIsSufficient_ShouldTransferAmount() {
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
    public void transfer_NonExistingAccount_ShouldThrowException() {
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
    public void transfer_NotFoundAccount_ShouldThrowException() {
        Long toAccountId = 2L; //fromAccount = account
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
    public void transfer_InsufficientBalance_ShouldThrowException() {
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
    public void withdraw_AccountExistsAndFundsAreSufficient_ShouldReduceBalance() {
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
    public void withdraw_InsufficientFunds_ShouldThrowException() {
        double withdrawalAmount = 9999999999.0;

        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(account.getId(), withdrawalAmount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("insufficient funds");

        verify(accountRepo, times(0)).save(any(Account.class));
    }
    @Test
    public void withdraw_AccountNotFound_ShouldThrowException() {
        double withdrawalAmount = 500.0;
        when(accountRepo.findById(account.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.withdraw(account.getId(), withdrawalAmount))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }
//    @Test
//    public void getAllAccounts_ExistingAccountDtos_ShouldReturnAccountsDtos(){
//        Account account2 = new Account(2L, "Peter", 20000.0);
//
//        when(accountRepo.findAll()).thenReturn(List.of(account, account2));
//        List<AccountDto> result = accountService.getAllAccounts();
//
//        assertThat(result).isNotNull();
//        assertEquals(2, result.size());
//
//        assertEquals("John", result.get(0).getAccountUsername());
//        assertEquals(50000.0, result.get(0).getBalance());
//        assertEquals("Peter", result.get(1).getAccountUsername());
//        assertEquals(20000.0, result.get(1).getBalance());
//    }
//    @Test
//    public void getAllAccounts_NonExistingAccounts_ShouldReturnEmptyList() {
//        // Arrange
//        when(accountRepo.findAll()).thenReturn(Collections.emptyList());
//
//        // Act
//        List<AccountDto> result = accountService.getAllAccounts();
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result).isEmpty();
//    }
    @Test
    public void deleteAccount_AccountExists_ShouldDeleteAccount() {
    Long accountId = 1L;
    Account account = new Account(accountId, "user123", 1000.0);

    when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));

    accountService.deleteAccount(accountId);

    verify(accountRepo, times(1)).findById(accountId);
    verify(accountRepo, times(1)).deleteById(accountId);
    }
    @Test
    public void deleteAccount_NonExistingAccount_ShouldThrowException() {

        assertThatThrownBy(()-> accountService.deleteAccount(account.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("account not found");
    }
}