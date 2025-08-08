package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Roles.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private Account account;
    private Account account2;
    private Users user1;
    private Users user2;
    private Users adminUser;
    private static final String ACCOUNT_API = "/api/v1/account/";

    @Autowired
    private DirectDebitRepo directDebitRepo;

    @BeforeEach
     void setUp() {
        user1 = Users.builder()
                .id(1L)
                .email("user1@gmail.com").password("password")
                .role(Role.USER)
                .build();
        user1 = userRepository.save(user1);

        user2 = Users.builder()
                .id(2L)
                .email("user2@gmail.com")
                .password("password")
                .role(Role.USER)
                .build();
        user2 = userRepository.save(user2);

        adminUser = Users.builder()
                .id(99L)
                .email("admin@gmail.com").password("password").role(Role.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        account = Account.builder()
                .id(1L)
                .accountUsername("account1").balance(1000.0).user(user1)
                .build();
        account = accountRepository.save(account);

        account2 = Account.builder()
                .id(2L).accountUsername("account2").balance(500.0).user(user2)
                .build();
        account2 = accountRepository.save(account2);
    }

    @Test
    void getAccountById_Integration_ReturnsAccount() throws Exception {
        ResultActions response = mockMvc.perform(get(  ACCOUNT_API + account.getId())
                .with(user("John").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountUsername").value("account1"))
                .andExpect(jsonPath("$.balance").value(1000.0));
    }

    @Test
    void getAccountById_Integration_NotFound() throws Exception {
        ResultActions response = mockMvc.perform(get(ACCOUNT_API + "999")
                .with(user("user").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound());
    }

    @Test
    void createAccount_Integration_ReturnsCreatedAccount() throws Exception {
        AccountDto newAccount = AccountDto.builder()
                .id(1L)
                .accountUsername("John").balance(100.0)
                .build();

        ResultActions response = mockMvc.perform(post(ACCOUNT_API + "create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAccount)));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountUsername").value("John"))
                .andExpect(jsonPath("$.balance").value(100.0));
    }
    @Test
    void getAccountById_Integration_Unauthorized() throws Exception {
        ResultActions response = mockMvc.perform(get(ACCOUNT_API + account.getId())
                .with(user("user").roles("USER")) // Different role
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isForbidden());
    }

    @Test
    void deposit_Integration_UpdatesBalanceSuccessfully() throws Exception {
        double depositAmount = 200.0;
        double expectedBalance = account.getBalance() + depositAmount;
        Map<String, Double> requestBody = Map.of("amount", depositAmount);

        ResultActions response = mockMvc.perform(patch(ACCOUNT_API + account.getId() + "/deposit")
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountUsername").value("account1"))
                .andExpect(jsonPath("$.balance").value(expectedBalance));

        Account updatedAccount = accountRepository.findById(account.getId()).get();
        assertThat(updatedAccount.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    void withdraw_Integration_UpdatesBalanceSuccessfully() throws Exception {
        double withdrawAmount = 100.0;
        double expectedBalance = account.getBalance() - withdrawAmount;
        Map<String, Double> requestBody = Map.of("amount", withdrawAmount);

        ResultActions response = mockMvc.perform(patch(ACCOUNT_API + account.getId() + "/withdraw")
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountUsername").value("account1"))
                .andExpect(jsonPath("$.balance").value(expectedBalance));

        Account updatedAccount = accountRepository.findById(account.getId()).get();
        assertThat(updatedAccount.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    void transfer_Integration_UpdatesBothAccountsSuccessfully() throws Exception {
        double transferAmount = 150.0;
        double expectedFromBalance = account.getBalance() - transferAmount;
        double expectedToBalance = account2.getBalance() + transferAmount;

        TransferRequest transferRequest = new TransferRequest(
                account.getId(), account2.getId(), transferAmount);

        ResultActions response = mockMvc.perform(patch(ACCOUNT_API + "/transfer")
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)));

        response.andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));

        Account updatedFromAccount = accountRepository.findById(account.getId()).get();
        Account updatedToAccount = accountRepository.findById(account2.getId()).get();

        assertThat(updatedFromAccount.getBalance()).isEqualTo(expectedFromBalance);
        assertThat(updatedToAccount.getBalance()).isEqualTo(expectedToBalance);
    }

    @Test
    void deleteAccount_Integration_RemovesAccountSuccessfully() throws Exception {
        Long accountIdToDelete = account.getId();

        ResultActions response = mockMvc.perform(delete(ACCOUNT_API + "delete/" + accountIdToDelete)
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().string("account successfuly deleted"));

        Optional<Account> deletedAccount = accountRepository.findById(accountIdToDelete);
        assertThat(deletedAccount).isEmpty();
    }

    @Test
    void createDirectDebit_Integration_CreatesDirectDebitSuccessfully() throws Exception {
        DirectDebit directDebit = DirectDebit.builder()
                .fromAccountId(account.getId()).toAccountId(account2.getId()).amount(50.0)
                .build();

        ResultActions response = mockMvc.perform(put(ACCOUNT_API + "dd/create")
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(directDebit)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.fromAccountId").value(account.getId()))
                .andExpect(jsonPath("$.toAccountId").value(account2.getId()))
                .andExpect(jsonPath("$.amount").value(50.0));

        List<DirectDebit> directDebits = directDebitRepo.findByActiveTrue();
        assertThat(directDebits).hasSize(1);
        DirectDebit savedDirectDebit = directDebits.get(0);
        assertThat(savedDirectDebit.getFromAccountId()).isEqualTo(account.getId());
        assertThat(savedDirectDebit.getToAccountId()).isEqualTo(account2.getId());
        assertThat(savedDirectDebit.getAmount()).isEqualTo(50.0);
        assertThat(savedDirectDebit.isActive()).isTrue();
    }

    @Test
    void cancelDirectDebit_Integration_CancelsDirectDebitSuccessfully() throws Exception {
        DirectDebit directDebit = DirectDebit.builder()
                .fromAccountId(account.getId())
                .toAccountId(account2.getId())
                .amount(50.0)
                .active(true)
                .build();
        DirectDebit savedDirectDebit = directDebitRepo.save(directDebit);

        ResultActions response = mockMvc.perform(put(ACCOUNT_API + "dd/cancel/" + savedDirectDebit.getId())
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().string("succesfully deleted"));

        DirectDebit updatedDirectDebit = directDebitRepo.findById(savedDirectDebit.getId()).get();
        assertThat(updatedDirectDebit.isActive()).isFalse();
    }

    @Test
    void getAllAccounts_Integration_ReturnsPagedAccountsSuccessfully() throws Exception {
        int pageNo = 0;
        int pageSize = 10;

        ResultActions response = mockMvc.perform(get(ACCOUNT_API + "/all")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .param("pageNo", String.valueOf(pageNo))
                .param("pageSize", String.valueOf(pageSize))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].accountUsername").value("account1"))
                .andExpect(jsonPath("$.content[1].accountUsername").value("account2"))
                .andExpect(jsonPath("$.pageNo").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        Page<Account> accounts = accountRepository.findAll(PageRequest.of(pageNo, pageSize));
        assertThat(accounts.getContent()).hasSize(2);
        assertThat(accounts.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getAccountById_Integration_ReturnsAccountSuccessfully() throws Exception {
        ResultActions response = mockMvc.perform(get(ACCOUNT_API + account.getId())
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountUsername").value("account1"))
                .andExpect(jsonPath("$.balance").value(1000.0));

        Account retrievedAccount = accountRepository.findById(account.getId()).get();
        assertThat(retrievedAccount.getAccountUsername()).isEqualTo("account1");
        assertThat(retrievedAccount.getBalance()).isEqualTo(1000.0);
    }

    @Test
    void createAccount_Integration_CreatesAccountSuccessfully() throws Exception {
        AccountDto newAccountDto = AccountDto.builder()
                .id(1L).accountUsername("John").balance(100.0)
                .build();

        ResultActions response = mockMvc.perform(post(ACCOUNT_API + "create")
                .with(user(user1.getEmail()).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAccountDto)));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountUsername").value("John"))
                .andExpect(jsonPath("$.balance").value(100.0));

        Optional<Account> createdAccount = accountRepository.findByAccountUsername("John");
        assertThat(createdAccount).isPresent();
        assertThat(createdAccount.get().getAccountUsername()).isEqualTo("John");
        assertThat(createdAccount.get().getBalance()).isEqualTo(100.0);
    }
}
