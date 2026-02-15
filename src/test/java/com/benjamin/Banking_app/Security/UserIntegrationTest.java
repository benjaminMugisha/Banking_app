package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.DirectDebit.DirectDebit;
import com.benjamin.Banking_app.DirectDebit.DirectDebitRepo;
import com.benjamin.Banking_app.Loans.Loan;
import com.benjamin.Banking_app.Loans.LoanRepository;
import com.benjamin.Banking_app.Transactions.Transaction;
import com.benjamin.Banking_app.Transactions.TransactionRepository;
import com.benjamin.Banking_app.Transactions.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")

class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private DirectDebitRepo directDebitRepo;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Account account;
    private Users user;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .firstName("User").lastName("User")
                .email("user@gmail.com")
                .password(passwordEncoder.encode("Password1234"))
                .role(Role.USER)
                .active(true)
                .build();

        account = Account.builder()
//                .accountUsername("userAccount")
                .balance(BigDecimal.valueOf(1000))
                .iban(IbanGenerator.generateIban())
                .user(user)
                .build();
        user.setAccount(account);
        userRepository.save(user);

        Users toAccountUser = Users.builder()
                .firstName("toAccount").lastName("User")
                .email("toAccount@gmail.com")
                .password(passwordEncoder.encode("Password1234"))
                .role(Role.USER).build();
        Account toAccount = Account.builder()
//                .accountUsername("toAccount")
                .balance(BigDecimal.valueOf(500)).user(toAccountUser)
                .iban(IbanGenerator.generateIban())
                .build();
        toAccountUser.setAccount(toAccount);
        userRepository.save(toAccountUser);

        userToken = jwtService.generateToken(user);

        DirectDebit debit = DirectDebit.builder()
                .fromAccount(account).toAccount(toAccount)
                .amount(BigDecimal.valueOf(50))
                .active(true)
                .build();
        directDebitRepo.save(debit);

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(BigDecimal.valueOf(200))
                .type(TransactionType.DEPOSIT)
                .time(LocalDate.now())
                .build();
        transactionRepository.save(transaction);

        Loan loan = Loan.builder()
                .account(account)
                .principal(BigDecimal.valueOf(500))
                .remainingBalance(BigDecimal.valueOf(300))
                .amountToPayEachMonth(BigDecimal.valueOf(100))
                .startDate(LocalDate.now().minusMonths(1))
                .nextPaymentDate(LocalDate.now().plusMonths(1))
                .active(true)
                .build();
        loanRepository.save(loan);
    }

    @Test
    void register_shouldPersistUserAndReturnTokens() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe",
                BigDecimal.valueOf(1000),
                "john.doe@mail.com",
                "Password123"
        );

        mockMvc.perform(post("/api/v2/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
//                .andExpect(jsonPath("$.accountUsername").value("johndoe123"));

        assertTrue(userRepository.existsByEmail("john.doe@mail.com"));
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("user@gmail.com", "Password1234");

        mockMvc.perform(post("/api/v2/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
//                .andExpect(jsonPath("$.accountUsername").value("userAccount"));
    }

    @Test
    void getMe_shouldReturnUserInfo() throws Exception {
        String token = jwtService.generateToken(user);
        mockMvc.perform(get("/api/v2/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.firstname").value("User"))
                .andExpect(jsonPath("$.lastname").value("User"))
//                .andExpect(jsonPath("$.accountUsername").value("userAccount"))
                .andExpect(jsonPath("$.accountBalance").value(1000));
    }

}
