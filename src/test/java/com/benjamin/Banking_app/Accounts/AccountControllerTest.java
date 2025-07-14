package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Loans.LoanServiceImpl;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Transactions.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@EnableMethodSecurity(prePostEnabled = true)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String ACCOUNT_API = "/api/v1/account/";
    @MockBean
    private AccountServiceImpl accountService;
    @MockBean
    private LoanServiceImpl loanService;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private Account account;
    private AccountDto accountDto;
    @BeforeEach
    void setUp() {
        account = Account.builder().accountUsername("John").balance(100.0)
                .build();
        accountDto = AccountDto.builder().accountUsername("John").balance(100.0)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAccounts_withAdminRole_ReturnsPagedAccounts() throws Exception {
        AccountDto acc2 = new AccountDto(2L, "user2", 200.0);

        AccountResponse accountResponse = new AccountResponse(
                List.of(accountDto, acc2), // accounts
                0,                   // pageNo
                2,                   // pageSize
                2L,                  // totalElements
                1,                   // totalPages
                true                 // last page
        );

        when(accountService.getAllAccounts(0, 2)).thenReturn(accountResponse);

        mockMvc.perform(get(ACCOUNT_API + "all")
                        .param("pageNo", "0")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(accountDto.getId()))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(accountService, times(1)).getAllAccounts(0, 2);
    }
    @Test
    void createAccount_ValidAccountAndPublic_ReturnsCreatedAccount() throws Exception {
        given(accountService.createAccount(ArgumentMatchers.any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        ResultActions response = mockMvc.perform(post(ACCOUNT_API + "create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDto)));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountUsername", CoreMatchers.is(accountDto.getAccountUsername())))
                .andExpect(jsonPath("$.balance", CoreMatchers.is(accountDto.getBalance())));
        verify(accountService, times(1)).createAccount(accountDto);
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void getAccountById_ValidAccountWithAdminRole_ReturnAccountDto() throws Exception {
        Long accountId = 1L;
        when(accountService.getAccountById(accountId)).thenReturn(accountDto);
        ResultActions response = mockMvc.perform(get(ACCOUNT_API + "{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountUsername", CoreMatchers.is(accountDto.getAccountUsername())))
                .andExpect(jsonPath("$.balance", CoreMatchers.is(accountDto.getBalance())));
        verify(accountService, times(1)).getAccountById(accountId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAccountById_ValidAccount_ReturnAccountDto() throws Exception {
        long accountId = 1L;
        doNothing().when(accountService).deleteAccount(1L);
        ResultActions response = mockMvc.perform(delete(ACCOUNT_API + "delete/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk());
        verify(accountService, times(1)).deleteAccount(accountId);
    }

    @Test
    void deposit_ValidAccountAndAmount_ReturnUpdatedAccount() throws Exception {
        long accountId = 1L;
        double depositAmount = 50.0;
        double newBalance = accountDto.getBalance() + depositAmount;
        AccountDto updatedAccountDto = AccountDto.builder()
                .accountUsername("John").balance(newBalance)
                .build();
        when(accountService.deposit(accountId, depositAmount)).thenReturn(updatedAccountDto);

        ResultActions response = mockMvc.perform(put(ACCOUNT_API + "{id}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": " + depositAmount + "}"));

        response.andExpect(status().isOk()) // Expecting 200 OK response
                .andExpect(jsonPath("$.accountUsername", CoreMatchers.is(accountDto.getAccountUsername())))
                .andExpect(jsonPath("$.balance", CoreMatchers.is(updatedAccountDto.getBalance())));

        verify(accountService, times(1)).deposit(accountId, depositAmount);
    }

    @Test
    @WithMockUser
    void withdraw_ValidAccountAndAmount_ReturnUpdatedAccount() throws Exception {
        long accountId = 1L;
        double initialBalance = 200.0;
        double withdrawAmount = 50.0;
        double newBalance = initialBalance - withdrawAmount;

        AccountDto updatedAccountDto = AccountDto.builder()
                .accountUsername("John").balance(newBalance)
                .build();
        when(accountService.withdraw(accountId, withdrawAmount)).thenReturn(updatedAccountDto);

        ResultActions response = mockMvc.perform(put(ACCOUNT_API + "{id}/withdraw", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": " + withdrawAmount + "}"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountUsername", CoreMatchers.is(updatedAccountDto.getAccountUsername())))
                .andExpect(jsonPath("$.balance", CoreMatchers.is(newBalance)));
        verify(accountService, times(1)).withdraw(accountId, withdrawAmount);
    }

    @Test
    @WithMockUser
    void transfer_ValidRequest_ReturnsSuccessMessage() throws Exception {
        TransferRequest transferRequest = new TransferRequest(1L, 2L, 10.0);

        doNothing().when(accountService).transfer(any(TransferRequest.class));

        ResultActions response = mockMvc.perform(put(ACCOUNT_API + "user/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)));

        response.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Transfer successful"));
        verify(accountService, times(1)).transfer(any(TransferRequest.class));
    }


    @Test
    void createDirectDebit_ValidRequest_ReturnsDirectDebit() throws Exception {
        DirectDebit dd = DirectDebit.builder()
                .fromAccountId(1L).toAccountId(2L).amount(100.0)
                .build();

        when(accountService.createDirectDebit(dd.getFromAccountId(), dd.getToAccountId(), dd.getAmount()))
                .thenReturn(dd);

        ResultActions response = mockMvc.perform(post(ACCOUNT_API + "dd/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dd)));

        response.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fromAccountId").value(dd.getFromAccountId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.toAccountId").value(dd.getToAccountId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(dd.getAmount()));

        verify(accountService, times(1)).createDirectDebit(dd.getFromAccountId(), dd.getToAccountId(), dd.getAmount());
    }

    @Test
    void cancelDirectDebit_ValidId_ReturnsSuccessMessage() throws Exception {
        Long id = 1L;

        doNothing().when(accountService).cancelDirectDebit(id);

        ResultActions response = mockMvc.perform(put(ACCOUNT_API + "dd/cancel/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("succesfully deleted"));

        verify(accountService, times(1)).cancelDirectDebit(id);
    }
}
