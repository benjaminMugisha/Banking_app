package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Loans.LoanServiceImpl;
import com.benjamin.Banking_app.Roles.Role;
import com.benjamin.Banking_app.Transactions.TransactionRepository;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthenticationController Security Tests")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    UserRepository userRepository;
    @MockBean
    TransactionServiceImpl transactionService;
    @MockBean
    AccountServiceImpl accountService;
    @MockBean
    LoanServiceImpl loanService;
    @MockBean
    TransactionRepository transactionRepository;

    private static final String AUTH_API = "/api/v1/auth/";
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("john@example.com").password("password")
                .firstName("John").lastName("Doe")
                .accountUsername("john_doe").balance(100.0).role(Role.USER)
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("john@example.com").password("password")
                .build();

        Account account = Account.builder()
                .accountUsername("john_doe").balance(100.0)
                .build();

        authenticationResponse = AuthenticationResponse.builder()
                .token("fake-jwt-token").account(account)
                .build();
    }

    @Test
    void register_ValidRequest_ReturnsToken() throws Exception {
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post(AUTH_API + "register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.account.accountUsername").value("john_doe"))
                .andExpect(jsonPath("$.account.balance").value(100.0));

        verify(authenticationService, times(1)).register(registerRequest);
    }

    @Test
    void authenticate_ValidCredentials_ReturnsToken() throws Exception {
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        mockMvc.perform(post(AUTH_API + "authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.account.accountUsername").value("john_doe"))
                .andExpect(jsonPath("$.account.balance").value(100.0));

        verify(authenticationService, times(1)).authenticate(authenticationRequest);
    }

    @Test
    void delete_ValidUserId_ReturnsOk() throws Exception {
        int userId = 1;
        doNothing().when(userRepository).deleteById(userId);

        mockMvc.perform(put(AUTH_API + "{id}", userId))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).deleteById(userId);
    }
}
