package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LoanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Account account;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
        accountRepository.deleteAll();

        Users user = Users.builder()
                .firstName("Test")
                .lastName("User")
                .email("testloan@mail.com")
                .password(passwordEncoder.encode("Password123"))
                .active(true)
                .role(Role.USER)
                .build();

        account = Account.builder()
                .accountUsername("loanUserAcc")
                .balance(BigDecimal.valueOf(5000))
                .iban(IbanGenerator.generateIban())
                .user(user)
                .build();
        user.setAccount(account);
        userRepository.save(user);

        userToken = jwtService.generateToken(user);

        Loan loan = Loan.builder()
                .account(account)
                .principal(BigDecimal.valueOf(1000))
                .remainingBalance(BigDecimal.valueOf(1200))
                .amountToPayEachMonth(BigDecimal.valueOf(100))
                .startDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusDays(30))
                .active(true)
                .build();
        loanRepository.save(loan);
    }

    @Test
    void getLoanByLoanId_ShouldReturnLoan() throws Exception {
        Loan loan = loanRepository.findAll().get(0);

        mockMvc.perform(get("/api/v2/loans/" + loan.getLoanId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(loan.getLoanId()))
                .andExpect(jsonPath("$.principal").value(1000));
    }

    @Test
    void applyForLoan_ShouldCreateLoan() throws Exception {
        LoanRequest request = new LoanRequest(
                BigDecimal.valueOf(200000),
                BigDecimal.valueOf(100),
                12
        );

        mockMvc.perform(post("/api/v2/loans/apply")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanDto.principal").value(100));
    }

    @Test
    void repayFullLoanEarly_ShouldMarkLoanInactive() throws Exception {
        Loan loan = loanRepository.findAll().get(0);

        mockMvc.perform(patch("/api/v2/loans/repay-full/" + loan.getLoanId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Loan of €1200 fully repaid. your remaining balance is: €3800"));
    }

    @Test
//    @WithMockUser(username = "loanUser@mail.com", roles = {"USER"})
    void getLoansByAccountId_ShouldReturnLoans() throws Exception {
        mockMvc.perform(get("/api/v2/loans/account")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("accountUsername", "loanUserAcc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].principal").value(1000));
    }
}

//@Test
//@WithMockUser
//void getLoans_shouldReturnLoanPageResponse() throws Exception {
//    LoanPageResponse response = LoanPageResponse.builder()
//            .pageNo(0)
//            .pageSize(10)
//            .content(List.of())
//            .totalPages(1)
//            .build();
//
//    when(loanService.getLoansOfAnAccount(0, 10, null)).thenReturn(response);
//
//    mockMvc.perform(get("/api/v2/auth/me/loans")
//                    .param("pageNo", "0")
//                    .param("pageSize", "10"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.pageNo").value(0))
//            .andExpect(jsonPath("$.pageSize").value(10));
//}

//@Test
//void getLoans_shouldReturnForbiddenWithoutUser() throws Exception {
//    mockMvc.perform(get("/api/v2/auth/me/direct-debits"))
//            .andExpect(status().isForbidden());
//}