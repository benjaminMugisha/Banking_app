package com.benjamin.Banking_app;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createInitialAdmin() {

        if (userRepository.existsByEmail("adminemail@mybank.com")) {
            return;
        }
        Users admin = Users.builder()
                .email("adminemail@mybank.com")
                .password(passwordEncoder.encode("Admin12345"))
                .firstName("System").lastName("Admin")
                .role(Role.ADMIN)
                .build();
        Account account = Account.builder()
                .user(admin).balance(BigDecimal.valueOf(500))
                .iban("IE29BENJ94125634197592")
                .build();
        userRepository.save(admin);
        accountRepository.save(account);
    }
}
