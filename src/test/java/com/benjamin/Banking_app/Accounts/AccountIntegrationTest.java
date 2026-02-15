package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.DirectDebit.DirectDebitRepo;
import com.benjamin.Banking_app.Security.IbanGenerator;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")

public class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private DirectDebitRepo directDebitRepo;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private Account account;
    private Account account2;
    private Users user1;
    private Users user2;

    private static final String ACCOUNT_API = "/api/v2/accounts/";

    @BeforeEach
    void setUp() {
        user1 = Users.builder().firstName("User1").lastName("User1")
                .email("user1@gmail.com").password("Password1234")
                .role(Role.USER).build();
        user1 = userRepository.save(user1);

        user2 = Users.builder().firstName("User2").lastName("User2")
                .email("user2@gmail.com").password("Password1234")
                .role(Role.USER).build();
        user2 = userRepository.save(user2);

        account = Account.builder()
//                .accountUsername("account1").
                .balance(BigDecimal.valueOf(1000.0))
                .iban(IbanGenerator.generateIban())
                .user(user1)
                .build();
        account = accountRepository.save(account);
        user1.setAccount(account);
        userRepository.save(user1);

        account2 = Account.builder()
//                .accountUsername("account2")
                .balance(BigDecimal.valueOf(500.0))
                .iban(IbanGenerator.generateIban())
                .user(user2)
                .build();
        account2 = accountRepository.save(account2);
        user2.setAccount(account2);
        userRepository.save(user2);
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void getAccountById_ShouldReturnAccount() throws Exception {
        mockMvc.perform(get(ACCOUNT_API + account.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accountUsername").value("account1"))
                .andExpect(jsonPath("$.balance").value(1000.0));
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void deposit_ShouldUpdateBalance() throws Exception {
        double depositAmount = 200.0;
        Map<String, Double> request = Map.of("amount", depositAmount);

        mockMvc.perform(patch(ACCOUNT_API + "deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Deposit of €200.0 successful. your new balance is: €1200.0"));

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(BigDecimal.valueOf(1200.0));
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void withdraw_ShouldReduceBalance() throws Exception {
        double withdrawAmount = 300.0;
        Map<String, Double> request = Map.of("amount", withdrawAmount);

        mockMvc.perform(patch(ACCOUNT_API + "withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Withdraw of €300.0 successful. your new balance is: €700.0"));

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualTo(BigDecimal.valueOf(700.0));
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void transfer_ShouldUpdateBothAccounts() throws Exception {
        Map<String, Object> transferRequest = new HashMap<>();
        transferRequest.put("toIban", account2.getIban());
        transferRequest.put("amount", BigDecimal.valueOf(400.0));

        mockMvc.perform(patch(ACCOUNT_API + "transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer of €400.0 successful. " +
                        "your remaining balance is €600.0"));

        Account updatedFrom = accountRepository.findById(account.getId()).orElseThrow();
        Account updatedTo = accountRepository.findById(account2.getId()).orElseThrow();
        assertThat(updatedFrom.getBalance()).isEqualTo(BigDecimal.valueOf(600.0));
        assertThat(updatedTo.getBalance()).isEqualTo(BigDecimal.valueOf(900.0));
    }

//    @Test
//    @WithMockUser(username = "user1@gmail.com", roles = {"ADMIN"})
//    void deleteAccount_ShouldRemoveAccount() throws Exception {
//        mockMvc.perform(delete(ACCOUNT_API + "delete/" + account.getId())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("account successfuly deleted"));
//
//        assertThat(accountRepository.findById(account.getId())).isEmpty();
//    }
}
