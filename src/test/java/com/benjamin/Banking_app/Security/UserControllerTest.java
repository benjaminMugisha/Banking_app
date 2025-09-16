package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.DirectDebit.DirectDebitResponse;
import com.benjamin.Banking_app.DirectDebit.DirectDebitServiceImpl;
import com.benjamin.Banking_app.Loans.LoanPageResponse;
import com.benjamin.Banking_app.Loans.LoanServiceImpl;
import com.benjamin.Banking_app.Transactions.TransactionResponse;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private DirectDebitServiceImpl directDebitService;
    @MockBean
    private TransactionServiceImpl transactionService;
    @MockBean
    private LoanServiceImpl loanService;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AccountRepository accountRepository;

    @Test
    void register_shouldReturnAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe",
                "johndoe1234",
                BigDecimal.valueOf(1000), "johndoe@gmail.com",
                "Password12345", Role.USER);
        AuthenticationResponse response = AuthenticationResponse.builder().
                token("token123").refreshToken("refresh123")
                .accountUsername("johnAccount")
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v2/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.accountUsername").value("johnAccount"));
    }

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("john@mail.com", "1234");
        AuthenticationResponse response = AuthenticationResponse.builder().token("token123").refreshToken("refresh123").accountUsername("johnAccount").build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v2/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void refreshToken_shouldReturnNewToken() throws Exception {
        Users user = Users.builder().id(1L).email("john@mail.com").password("pw").role(Role.USER).build();
        user.setAccount(Account.builder().accountUsername("johnAccount").balance(BigDecimal.valueOf(500)).user(user).build());

        when(jwtService.extractUserName("refresh123")).thenReturn("john@mail.com");
        when(userRepository.findByEmail("john@mail.com")).thenReturn(java.util.Optional.of(user));
        when(jwtService.isTokenValid("refresh123", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newToken");

        mockMvc.perform(post("/api/v2/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "refresh123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newToken"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"));
    }

    @Test
    void getMe_shouldReturnUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .email("john@mail.com")
                .firstname("John")
                .lastname("Doe")
                .accountUsername("johnAccount")
                .accountBalance(BigDecimal.valueOf(1000))
                .build();

        when(authenticationService.getUserInfo()).thenReturn(userDto);

        mockMvc.perform(get("/api/v2/auth/me")
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }


    @Test
    @WithMockUser
    void getDirectDebits_shouldReturnDirectDebitResponse() throws Exception {
        DirectDebitResponse response = DirectDebitResponse.builder()
                .pageNo(0)
                .pageSize(10)
                .totalPages(1)
                .content(List.of())
                .build();

        when(directDebitService.getDirectDebits(0, 10, null)).thenReturn(response);

        mockMvc.perform(get("/api/v2/auth/me/direct-debits")
                        .param("pageNo", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getDirectDebits_shouldReturnForbiddenWithoutUser() throws Exception {
        mockMvc.perform(get("/api/v2/auth/me/direct-debits"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser
    void getTransactions_shouldReturnTransactionResponse() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .pageNo(0)
                .pageSize(10)
                .content(List.of())
                .totalPages(1)
                .build();

        when(transactionService.transactions(0, 10, null)).thenReturn(response);

        mockMvc.perform(get("/api/v2/auth/me/transactions")
                        .param("pageNo", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getTransactions_shouldReturnForbiddenWithoutUser() throws Exception {
        mockMvc.perform(get("/api/v2/auth/me/direct-debits"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser
    void getLoans_shouldReturnLoanPageResponse() throws Exception {
        LoanPageResponse response = LoanPageResponse.builder()
                .pageNo(0)
                .pageSize(10)
                .content(List.of())
                .totalPages(1)
                .build();

        when(loanService.getLoansOfAnAccount(0, 10, null)).thenReturn(response);

        mockMvc.perform(get("/api/v2/auth/me/loans")
                        .param("pageNo", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getLoans_shouldReturnForbiddenWithoutUser() throws Exception {
        mockMvc.perform(get("/api/v2/auth/me/direct-debits"))
                .andExpect(status().isForbidden());
    }
}
