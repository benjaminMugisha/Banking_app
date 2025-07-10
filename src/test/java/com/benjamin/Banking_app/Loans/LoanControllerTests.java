package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountController;
import com.benjamin.Banking_app.Accounts.AccountService;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Transactions.TransactionController;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)

public class LoanControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService accountService;
    @MockBean
    private LoanServiceImpl loanService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private LoanRepository loanRepository;
    @MockBean
    private TransactionController transactionController;
    @MockBean
    private TransactionServiceImpl transactionService;
    @Autowired
    private ObjectMapper objectMapper;

    private Loan loan;
    private Loan loan2;
    private Account account;
    private long loanId;

    @BeforeEach
    void setUp() {
        loanId = 1L;
        loan = Loan.builder()
                .LoanId(1L).account(account).remainingBalance(10.0).startDate(LocalDateTime.now())
                .principal(15.0).amountToPayEachMonth(1)
                .build();
        loan2 = Loan.builder().LoanId(2L).account(account).principal(2.0).build();
        account = Account.builder()
                .id(1L).accountUsername("John").balance(100.0)
                .build();
    }
    @Test
    @WithMockUser(username="User",roles={"USER"})
    void applyForLoan_ValidRequest_ReturnsCreated() throws Exception {
        LoanRequest request = new LoanRequest(1L, 100.0, 1.0, 12);
        LoanResponse response = new LoanResponse("loan accepted", loan);

        when(loanService.applyForLoan(any(LoanRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("loan accepted"))
                .andExpect(jsonPath("$.loan.principal").value(15.0));

        verify(loanService, times(1)).applyForLoan(any(LoanRequest.class));
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    void getAllLoans_ReturnsListOfLoans() throws Exception {

        List<Loan> loans = List.of(loan, loan2);
        LoanPageResponse pageResponse = LoanPageResponse.builder().pageSize(10).last(true).pageNo(1)
                .content(loans).build();
        when(loanService.getAllLoans(1, 10)).thenReturn(pageResponse);

        ResultActions response = mockMvc.perform(get("/api/v1/loan/all")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo", "1")
                .param("pageSize", "10"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN", "USER"})
    void getLoanByLoanId_LoanExists_ReturnsLoan() throws Exception {
        when(loanService.getLoanByLoanId(1L)).thenReturn(loan);

        mockMvc.perform(get("/api/v1/loan/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanId").value(1L))
                .andExpect(jsonPath("$.principal").value(15.0));

        verify(loanService, times(1)).getLoanByLoanId(1L);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMIN", "USER"})
    void getLoansByAccountId_LoansExist_ReturnsLoans() throws Exception {
        long accountId = 1L;
        List<Loan> loans = List.of(loan, loan2);

        when(loanService.getLoansByAccountId(accountId)).thenReturn(loans);
        mockMvc.perform(get("/api/v1/loan/account/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].loanId").value(1L))
                .andExpect(jsonPath("$[1].loanId").value(2L));

        verify(loanService, times(1)).getLoansByAccountId(accountId);
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    void monthlyRepay_ValidAmount_ReturnsLoanResponse() throws Exception {
        double amount = 500.0;
        LoanResponse loanResponse = new LoanResponse("Loan repayment successful", null);

        when(loanService.processMonthlyRepayment(loanId, amount)).thenReturn(loanResponse);

        mockMvc.perform(put("/api/v1/loan/{loanId}/repay", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": " + amount + "}"))
                .andExpect(status().isOk()) // Expecting status 200
                .andExpect(jsonPath("$.message").value("Loan repayment successful"))
                .andExpect(jsonPath("$.loan").isEmpty()); // Assuming loan field is empty in the response

        verify(loanService, times(1)).processMonthlyRepayment(loanId, amount);
    }

    @Test
    @WithMockUser(username="admin",roles ={"ADMIN"})
    void deleteLoan_LoanExists_ReturnsSuccessMessage() throws Exception {
        doNothing().when(loanService).deleteLoan(loanId);

        mockMvc.perform(delete("/api/v1/loan/delete/{loanId}", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expecting status 200
                .andExpect(content().string("loan successfully deleted"));

        verify(loanService, times(1)).deleteLoan(loanId);
    }

    @Test
    @WithMockUser(username = "ADMIN", roles = {"ADMIN"})
    void deleteLoan_LoanNotFound_ThrowsEntityNotFoundException() throws Exception {
        doThrow(new EntityNotFoundException("loan with id: " + loanId + " not found"))
                .when(loanService).deleteLoan(loanId);

        mockMvc.perform(delete("/api/v1/loan/delete/{loanId}", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expecting status 404
                .andExpect(jsonPath("$.message").value("loan with id: " + loanId + " not found"));

        verify(loanService, times(1)).deleteLoan(loanId);
    }

    @Test
    void repayFullLoanEarly_LoanExists_SuccessfulRepayment() throws Exception {
        LoanResponse loanResponse = new LoanResponse("Loan fully repaid early successfully, 2% penalty applied");
        when(loanService.repayLoanEarly(loanId)).thenReturn(loanResponse);

        mockMvc.perform(put("/api/v1/loan/{loanId}/repayFull", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expecting status 200
                .andExpect(jsonPath("$.message").value("Loan fully repaid early successfully, 2% penalty applied"));

        verify(loanService, times(1)).repayLoanEarly(loanId);
    }

    @Test
    void repayFullLoanEarly_LoanExists_InsufficientFunds() throws Exception {
        loan.setRemainingBalance(1000);

        LoanResponse loanResponse =
                new LoanResponse("insufficient funds to clear the loan with the 2% penalty included.", null);
        when(loanService.repayLoanEarly(loanId)).thenReturn(loanResponse);

        mockMvc.perform(put("/api/v1/loan/{loanId}/repayFull", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expecting status 200
                .andExpect(jsonPath("$.message").value("insufficient funds to clear the loan with the 2% penalty included."));

        verify(loanService, times(1)).repayLoanEarly(loanId);
    }

    @Test
    void repayFullLoanEarly_LoanNotFound_ThrowsEntityNotFoundException() throws Exception {
        when(loanService.repayLoanEarly(loanId)).thenThrow(new EntityNotFoundException("loan with id: " + loanId + " not found"));

        mockMvc.perform(put("/api/v1/loan/{loanId}/repayFull", loanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expecting status 404
                .andExpect(jsonPath("$.message").value("loan with id: " + loanId + " not found"));

        verify(loanService, times(1)).repayLoanEarly(loanId);
    }
}