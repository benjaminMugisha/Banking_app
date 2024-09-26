package com.benjamin.Banking_app.ServiceTest;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.benjamin.Banking_app.Dto.AccountDto;
import com.benjamin.Banking_app.Dto.TransferRequest;
import com.benjamin.Banking_app.Entity.Account;
import com.benjamin.Banking_app.Mapper.AccountMapper;
import com.benjamin.Banking_app.Repository.AccountRepo;
import static org.assertj.core.api.Assertions.*;
import com.benjamin.Banking_app.Service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

public class AccountServiceImplTest {

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private void testAccountNotFoundForMethod(Runnable method, Long accountId) {
        // Mock the repository behavior for non-existing account
        when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(method::run)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("account does not exist");

        // Verify that the repo's deleteById or save method was never called
        verify(accountRepo, times(0)).deleteById(anyLong());
        verify(accountRepo, times(0)).save(any(Account.class));
    }

    @Test
    public void createAccount_ShouldReturnSavedAccountDto() {
        // Arrange
        AccountDto accountDto = new AccountDto(1L, "Benjamin", 50000.0);
        Account account = AccountMapper.MapToAccount(accountDto);
        Account savedAccount = Account.builder()
                .id(1L)
                .accountUsername("Benjamin")
                .balance(50000.0)
                .build();

        when(accountRepo.save(account)).thenReturn(savedAccount);

        AccountDto result = accountService.createAccount(accountDto);

        assertThat(result).isNotNull();
        assertThat(result.getAccountUsername()).isEqualTo("Benjamin");
        assertThat(result.getBalance()).isEqualTo(50000.0);

        verify(accountRepo, times(1)).save(account);
    }
        @Test
        public void getAccountById_ShouldReturnAccountDto_WhenAccountExists() {

            Long accountId = 1L;
            Account account = new Account(accountId, "user123", 1000.0);
            AccountDto expectedAccountDto = new AccountDto(accountId, "user123", 1000.0);

            when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));

            AccountDto result = accountService.getAccountById(accountId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(expectedAccountDto.getId());
            assertThat(result.getAccountUsername()).isEqualTo(expectedAccountDto.getAccountUsername());
            assertThat(result.getBalance()).isEqualTo(expectedAccountDto.getBalance());

            verify(accountRepo, times(1)).findById(accountId);
        }

        @Test
        public void getAccountById_ShouldThrowException_WhenAccountDoesNotExist() {
            Long accountId = 1L;

            testAccountNotFoundForMethod(() -> accountService.deleteAccount(accountId), accountId);
        }

        @Test
        public void deposit_ShouldUpdateBalance_WhenAccountExists() {
            Long accountId = 1L;
            double initialBalance = 1000.0;
            double depositAmount = 500.0;
            double expectedBalance = initialBalance + depositAmount;

            Account account = new Account(accountId, "user123", initialBalance);
            Account updatedAccount = new Account(accountId, "user123", expectedBalance);
            AccountDto expectedAccountDto = new AccountDto(accountId, "user123", expectedBalance);

            when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepo.save(account)).thenReturn(updatedAccount);

            AccountDto result = accountService.deposit(accountId, depositAmount);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(expectedAccountDto.getId());
            assertThat(result.getAccountUsername()).isEqualTo(expectedAccountDto.getAccountUsername());
            assertThat(result.getBalance()).isEqualTo(expectedAccountDto.getBalance());

            verify(accountRepo, times(1)).findById(accountId);
            verify(accountRepo, times(1)).save(account);

            assertThat(account.getBalance()).isEqualTo(expectedBalance);
        }
        @Test
        public void deposit_ShouldThrowException_WhenAccountDoesNotExist() {
            Long accountId = 1L;
            double depositAmount = 500.0;

            when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deposit(accountId, depositAmount))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("account does not exist");

            verify(accountRepo, times(1)).findById(accountId);
            verify(accountRepo, times(0)).save(any(Account.class));  // Ensure save is not called
        }
        @Test
        public void transfer_ShouldTransferAmount_WhenAccountsExistAndBalanceIsSufficient() {
            Long fromAccountId = 1L;
            Long toAccountId = 2L;
            double transferAmount = 500.0;

            Account fromAccount = new Account(fromAccountId, "fromUser", 1000.0);
            Account toAccount = new Account(toAccountId, "toUser", 200.0);

            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromAccountId(fromAccountId);
            transferRequest.setToAccountId(toAccountId);
            transferRequest.setAmount((long) transferAmount);

            when(accountRepo.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
            when(accountRepo.findById(toAccountId)).thenReturn(Optional.of(toAccount));

            accountService.transfer(transferRequest);

            assertThat(fromAccount.getBalance()).isEqualTo(1000.0 - transferAmount);
            assertThat(toAccount.getBalance()).isEqualTo(200.0 + transferAmount);

            verify(accountRepo, times(1)).save(fromAccount);
            verify(accountRepo, times(1)).save(toAccount);
        }

        @Test
        public void transfer_ShouldThrowException_WhenFromAccountNotFound() {
            Long fromAccountId = 1L;
            Long toAccountId = 2L;
            double transferAmount = 500.0;

            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromAccountId(fromAccountId);
            transferRequest.setToAccountId(toAccountId);
            transferRequest.setAmount((long) transferAmount);

            when(accountRepo.findById(fromAccountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.transfer(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("From account not found");

            verify(accountRepo, times(0)).save(any(Account.class));
        }

        @Test
        public void transfer_ShouldThrowException_WhenToAccountNotFound() {
            Long fromAccountId = 1L;
            Long toAccountId = 2L;
            double transferAmount = 500.0;

            Account fromAccount = new Account(fromAccountId, "fromUser", 1000.0);

            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromAccountId(fromAccountId);
            transferRequest.setToAccountId(toAccountId);
            transferRequest.setAmount((long) transferAmount);

            when(accountRepo.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
            when(accountRepo.findById(toAccountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.transfer(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("To account not found");

            verify(accountRepo, times(0)).save(any(Account.class));
        }

        @Test
        public void transfer_ShouldThrowException_WhenInsufficientBalance() {
            Long fromAccountId = 1L;
            Long toAccountId = 2L;
            double transferAmount = 1500.0;

            Account fromAccount = new Account(fromAccountId, "fromUser", 1000.0);
            Account toAccount = new Account(toAccountId, "toUser", 200.0);

            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromAccountId(fromAccountId);
            transferRequest.setToAccountId(toAccountId);
            transferRequest.setAmount((long) transferAmount);

            when(accountRepo.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
            when(accountRepo.findById(toAccountId)).thenReturn(Optional.of(toAccount));

            assertThatThrownBy(() -> accountService.transfer(transferRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Insufficient balance");

            verify(accountRepo, times(0)).save(any(Account.class));
        }

        @Test
        public void withdraw_ShouldReduceBalance_WhenAccountExistsAndFundsAreSufficient() {
            Long accountId = 1L;
            double initialBalance = 1000.0;
            double withdrawalAmount = 500.0;
            double expectedBalance = initialBalance - withdrawalAmount;

            Account account = new Account(accountId, "user123", initialBalance);
            Account updatedAccount = new Account(accountId, "user123", expectedBalance);
            AccountDto expectedAccountDto = new AccountDto(accountId, "user123", expectedBalance);

            when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepo.save(account)).thenReturn(updatedAccount);

            AccountDto result = accountService.withdraw(accountId, withdrawalAmount);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(expectedAccountDto.getId());
            assertThat(result.getAccountUsername()).isEqualTo(expectedAccountDto.getAccountUsername());
            assertThat(result.getBalance()).isEqualTo(expectedAccountDto.getBalance());

            verify(accountRepo, times(1)).findById(accountId);
            verify(accountRepo, times(1)).save(account);

            assertThat(account.getBalance()).isEqualTo(expectedBalance);
        }

        @Test
        public void withdraw_ShouldThrowException_WhenAccountNotFound() {
            Long accountId = 1L;
            double withdrawalAmount = 500.0;

            testAccountNotFoundForMethod(() -> accountService.deleteAccount(accountId), accountId);

        }

        @Test
        public void withdraw_ShouldThrowException_WhenInsufficientFunds() {
            Long accountId = 1L;
            double initialBalance = 500.0;
            double withdrawalAmount = 1000.0;

            Account account = new Account(accountId, "user123", initialBalance);

            when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> accountService.withdraw(accountId, withdrawalAmount))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("insufficient funds");

            verify(accountRepo, times(0)).save(any(Account.class));
        }
    @Test
    public void deleteAccount_ShouldDeleteAccount_WhenAccountExists() {
        Long accountId = 1L;
        Account account = new Account(accountId, "user123", 1000.0);

        when(accountRepo.findById(accountId)).thenReturn(Optional.of(account));

        accountService.deleteAccount(accountId);

        verify(accountRepo, times(1)).findById(accountId);
        verify(accountRepo, times(1)).deleteById(accountId);
    }

    @Test
    public void deleteAccount_ShouldThrowException_WhenAccountDoesNotExist() {
        Long accountId = 1L;

        testAccountNotFoundForMethod(() -> accountService.deleteAccount(accountId), accountId);
    }
}



