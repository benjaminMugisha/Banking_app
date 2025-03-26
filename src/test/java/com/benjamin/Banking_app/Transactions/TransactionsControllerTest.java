package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.AccountController;
import com.benjamin.Banking_app.Accounts.AccountService;
import com.benjamin.Banking_app.Loans.LoanRepository;
import com.benjamin.Banking_app.Loans.LoanServiceImpl;
import com.benjamin.Banking_app.Security.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService accountService;
    @MockBean
    private LoanServiceImpl loanService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private TransactionServiceImpl transactionService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void getTransactionHistory_AccountHasTransactions_ReturnsTransactions() throws Exception {
        Long accountId = 1L;
        int pageNo = 0, pageSize = 10;

        TransactionDto t1 = TransactionDto.builder()
                .transactionId(1L).type("DEPOSIT").amount(50.0).timestamp(LocalDateTime.now())
                .build();
        TransactionDto t2 = TransactionDto.builder()
                .transactionId(2L).type("WITHDRAW").amount(10.0).timestamp(LocalDateTime.now())
                .build();

        List<TransactionDto> transactionDtos = Arrays.asList(t1, t2);

        TransactionResponse transactionResponse = TransactionResponse.builder()
                .content(transactionDtos).pageNo(pageNo).pageSize(pageSize)
                .totalElements(2L).totalPages(1).last(true)
                .build();
        when(transactionService.findByAccountId(accountId, pageNo, pageSize)).thenReturn(transactionResponse);

        mockMvc.perform(get("/api/v1/transactions/{accountId}", accountId)
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.content[1].type").value("WITHDRAW"))
                .andExpect(jsonPath("$.content[0].amount").value(50.0))
                .andExpect(jsonPath("$.content[1].amount").value(10.0))
                .andExpect(jsonPath("$.pageNo").value(pageNo))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(transactionService, times(1)).findByAccountId(accountId, pageNo, pageSize);
    }

    @Test
    public void getTransactionHistory_AccountHasNoTransactions_ReturnsNotFound() throws Exception {
        Long accountId = 1L;
        int pageNo = 0, pageSize = 10;

        TransactionResponse emptyResponse = TransactionResponse.builder()
                .content(Collections.emptyList()).pageNo(pageNo).pageSize(pageSize)
                .totalElements(0L).totalPages(0).last(true)
                .build();
        when(transactionService.findByAccountId(accountId, pageNo, pageSize)).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/transactions/{accountId}", accountId)
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(transactionService, times(1)).findByAccountId(accountId, pageNo, pageSize);
    }
}