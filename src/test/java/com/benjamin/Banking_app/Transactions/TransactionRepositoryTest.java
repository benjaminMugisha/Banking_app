package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Security.IbanGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByAccountId_shouldReturnTransactionsForThatAccount() {
        Account account = Account.builder()
                .balance(BigDecimal.valueOf(1000))
                .iban(IbanGenerator.generateIban())
                .build();
        accountRepository.save(account);

        Transaction tx1 = Transaction.builder()
                .account(account).type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(200))
                .time(LocalDateTime.now())
                .build();

        Transaction tx2 = Transaction.builder()
                .account(account).type(TransactionType.WITHDRAW)
                .amount(BigDecimal.valueOf(50))
                .time(LocalDateTime.now())
                .build();

        transactionRepository.save(tx1);
        transactionRepository.save(tx2);

        Page<Transaction> page = transactionRepository.findByAccountId(account.getId(),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
//        assertThat(page.getContent().get(0).getAccount()
//                .getAccountUsername()).isEqualTo("john");
    }
    @Test
    void findByAccountId_shouldReturnEmptyPage_whenNoTransactions() {
        Page<Transaction> page = transactionRepository.
                findByAccountId(999L, PageRequest.of(0, 10));
        assertThat(page).isEmpty();
    }

//    @Test
//    void getTransactionHistory_shouldReturnTransactions() throws Exception {
//        mockMvc.perform(get("/api/v2/auth/me/transactions")
//                        .header("Authorization", "Bearer " + userToken)
//                        .param("pageNo", "0")
//                        .param("pageSize", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].amount").value(200))
//                .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"));
//    }
//
//    @Test
//    void getTransactions_withoutToken_shouldReturnisForbidden() throws Exception {
//        mockMvc.perform(get("/api/v2/auth/me/transactions"))
//                .andExpect(status().isForbidden());
//    }
//    @Test
//    @WithMockUser
//    void getTransactions_shouldReturnTransactionResponse() throws Exception {
//        TransactionResponse response = TransactionResponse.builder()
//                .pageNo(0)
//                .pageSize(10)
//                .content(List.of())
//                .totalPages(1)
//                .build();
//
//        when(transactionService.transactions(0, 10, null)).thenReturn(response);
//
//        mockMvc.perform(get("/api/v2/auth/me/transactions")
//                        .param("pageNo", "0")
//                        .param("pageSize", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.pageNo").value(0))
//                .andExpect(jsonPath("$.pageSize").value(10));
//    }
}
