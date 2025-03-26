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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class) // so we can test webmvc(api) controllers.
@AutoConfigureMockMvc(addFilters = false) //circumvents spring security, so we don't need to add tokens to our controllers
//@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
//@WebMvcTest(AccountController.class)

//mockmvc makes it easy to perform get, post, delete and update actions on our actual controllers in a test environment.
// so mockmvc performs CRUD on s method, then in return mockmvc gives us back a response object or result action object
//then we can test this object to make sure it sent or received the correct info
//mockmvc.perform(get/("api/endpoint") gives us an object back we can test and make sure it's the correct info back.

public class AccountControllerTest {
    @Autowired // Simulates HTTP requests.
    private MockMvc mockMvc;
    @MockBean
    private AccountServiceImpl accountService;
    @MockBean
    private LoanServiceImpl loanService;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    // data binding between Java objects and JSON. so it plus in mockmvc and when we send data in api endpoint
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
    public void createAccount_ValidAccount_ReturnCreatedAccount() throws Exception {
        given(accountService.createAccount(ArgumentMatchers.any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        ResultActions response = mockMvc.perform(post("/api/v1/account/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDto))); //turns object into string so we dont worry about serialisation.

        response.andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountUsername", CoreMatchers.is(accountDto.getAccountUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance", CoreMatchers.is(accountDto.getBalance())));
        verify(accountService, times(1)).createAccount(accountDto);
    }

//    @Test
//    @WithMockUser(username="admin",roles={"ADMIN"})
//    public void getAllAccounts_ExistingAccounts_ReturnAccountsDto() throws Exception {
//        List<AccountDto> accountDtos = List.of(
//                accountDto,
//                new AccountDto(2L, "Peter", 200.0)
//        );
//        when(accountService.getAllAccounts()).thenReturn(accountDtos);
//
//        mockMvc.perform(get("/api/v1/account/all"))
//                .andExpect(status().isOk())//expect 200 OK
//                .andExpect(jsonPath("$.size()").value(2))//expecting 2 accounts
//                .andExpect(jsonPath("$[0].accountUsername").value("John"))
//                .andExpect(jsonPath("$[1].accountUsername").value("Peter"));
//        verify(accountService, times(1)).getAllAccounts();
//    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void getAccountById_ValidAccount_ReturnAccountDto() throws Exception {
        Long accountId = 1L;
        when(accountService.getAccountById(accountId)).thenReturn(accountDto);
        ResultActions response = mockMvc.perform(get("/api/v1/account/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountUsername", CoreMatchers.is(accountDto.getAccountUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance", CoreMatchers.is(accountDto.getBalance())));
        verify(accountService, times(1)).getAccountById(accountId);
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void deleteAccountById_ValidAccount_ReturnAccountDto() throws Exception {
        long accountId = 1L;
        doNothing().when(accountService).deleteAccount(1L);
        ResultActions response = mockMvc.perform(delete("/api/v1/account/delete/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk());
        verify(accountService, times(1)).deleteAccount(accountId);
    }

    @Test
    public void deposit_ValidAccountAndAmount_ReturnUpdatedAccount() throws Exception {
        long accountId = 1L; // Test account ID
        double depositAmount = 50.0; // Test deposit amount
        double newBalance = accountDto.getBalance() + depositAmount;
        AccountDto updatedAccountDto = AccountDto.builder()
                .accountUsername("John").balance(newBalance)
                .build();
        when(accountService.deposit(accountId, depositAmount)).thenReturn(updatedAccountDto); // Mock service method

        ResultActions response = mockMvc.perform(put("/api/v1/account/{id}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": " + depositAmount + "}")); // Sending the deposit amount in the request body

        response.andExpect(status().isOk()) // Expecting 200 OK response
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountUsername", CoreMatchers.is(accountDto.getAccountUsername()))) // Verify account username
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance", CoreMatchers.is(updatedAccountDto.getBalance()))); // Verify updated balance

        verify(accountService, times(1)).deposit(accountId, depositAmount);
    }
    @Test
    @WithMockUser(username="user",roles={"USER"})
    public void withdraw_ValidAccountAndAmount_ReturnUpdatedAccount() throws Exception {
        long accountId = 1L;
        double initialBalance = 200.0;
        double withdrawAmount = 50.0;
        double newBalance = initialBalance - withdrawAmount;

        AccountDto updatedAccountDto = AccountDto.builder()
                .accountUsername("John").balance(newBalance)
                .build();
        when(accountService.withdraw(accountId, withdrawAmount)).thenReturn(updatedAccountDto);

        ResultActions response = mockMvc.perform(put("/api/v1/account/{id}/withdraw", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": " + withdrawAmount + "}"));

        response.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountUsername", CoreMatchers.is(updatedAccountDto.getAccountUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance", CoreMatchers.is(newBalance)));
        verify(accountService, times(1)).withdraw(accountId, withdrawAmount);
    }
    @Test
    @WithMockUser(username="user",roles={"USER"})
    public void transfer_ValidRequest_ReturnsSuccessMessage() throws Exception {
        TransferRequest transferRequest =new TransferRequest(1L, 2L, 10.0);

        doNothing().when(accountService).transfer(any(TransferRequest.class));

        ResultActions response = mockMvc.perform(put("/api/v1/account/user/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest))); // Convert to JSON

        response.andExpect(status().isOk()) // Expect HTTP 200
                .andExpect(MockMvcResultMatchers.content().string("Transfer successful"));
        verify(accountService, times(1)).transfer(any(TransferRequest.class));
    }
}