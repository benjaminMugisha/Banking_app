package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional

public class DirectDebitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DirectDebitRepo directDebitRepo;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    private Users user1;
    private Users user2;
    private Account account1;
    private Account account2;

    private static final String DD_API = "/api/v2/dd/";

    @BeforeEach
    void setUp() {
        user1 = Users.builder()
                .firstName("user1").lastName("user1")
                .email("user1@gmail.com").password("Password1")
                .role(Role.USER)
                .build();
        user1 = userRepository.save(user1);

        user2 = Users.builder()
                .firstName("user2").lastName("user2")
                .email("user2@gmail.com").password("Password2")
                .role(Role.USER)
                .build();
        user2 = userRepository.save(user2);

        account1 = Account.builder()
                .accountUsername("account1")
                .iban(IbanGenerator.generateIban())
                .balance(BigDecimal.valueOf(1000.0)).user(user1)
                .build();
        account1 = accountRepository.save(account1);
        user1.setAccount(account1);
        userRepository.save(user1);

        account2 = Account.builder()
                .accountUsername("account2")
                .iban(IbanGenerator.generateIban())
                .balance(BigDecimal.valueOf(500.0)).user(user2)
                .build();
        account2 = accountRepository.save(account2);
        user2.setAccount(account2);
        userRepository.save(user2);
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void createDirectDebit_ShouldSaveDirectDebitAndTransferImmediately() throws Exception {
        DirectDebitRequest request = new DirectDebitRequest();
        request.setToIban(account2.getIban());
        request.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(post(DD_API + "create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        List<DirectDebit> activeDD = directDebitRepo.findByActiveTrueAndNextPaymentDate(LocalDate.now().plusDays(28));
        assertThat(activeDD).hasSize(1);

        Account updatedFrom = accountRepository.findById(account1.getId()).orElseThrow();
        Account updatedTo = accountRepository.findById(account2.getId()).orElseThrow();

        assertThat(updatedFrom.getBalance().compareTo( BigDecimal.valueOf(900.0))).isZero();
        assertThat(updatedTo.getBalance().compareTo(BigDecimal.valueOf(600.0))).isZero();

    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void getDirectDebitById_ShouldReturnDirectDebit() throws Exception {
        DirectDebit dd = DirectDebit.builder()
                .fromAccount(account1)
                .toAccount(account2)
                .amount(BigDecimal.valueOf(50.0))
                .nextPaymentDate(LocalDate.now().plusDays(28))
                .active(true)
                .build();
        dd = directDebitRepo.save(dd);

        mockMvc.perform(get(DD_API + "get/" + dd.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dd.getId()))
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void getActiveDirectDebits_ShouldReturnPagedDirectDebits() throws Exception {
        DirectDebit dd1 = DirectDebit.builder()
                .fromAccount(account1)
                .toAccount(account2)
                .amount(BigDecimal.valueOf(50.0))
                .nextPaymentDate(LocalDate.now().plusDays(28))
                .active(true)
                .build();
        DirectDebit dd2 = DirectDebit.builder()
                .fromAccount(account1)
                .toAccount(account2)
                .amount(BigDecimal.valueOf(75.0))
                .nextPaymentDate(LocalDate.now().plusDays(28))
                .active(true)
                .build();
        directDebitRepo.save(dd1);
        directDebitRepo.save(dd2);

        mockMvc.perform(get(DD_API + "get")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].amount").value(50.0))
                .andExpect(jsonPath("$.content[1].amount").value(75.0));
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", roles = {"USER"})
    void cancelDirectDebit_ShouldDeactivateDirectDebit() throws Exception {
        DirectDebit dd = DirectDebit.builder()
                .fromAccount(account1)
                .toAccount(account2)
                .amount(BigDecimal.valueOf(50.0))
                .nextPaymentDate(LocalDate.now().plusDays(28))
                .active(true)
                .build();
        dd = directDebitRepo.save(dd);

        mockMvc.perform(patch(DD_API + "cancel/" + dd.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Direct debit with id: " + dd.getId() + " has been cancelled"));

        DirectDebit updated = directDebitRepo.findById(dd.getId()).orElseThrow();
        assertThat(updated.isActive()).isFalse();
    }
}
