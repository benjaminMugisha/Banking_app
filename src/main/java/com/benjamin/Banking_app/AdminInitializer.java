package com.benjamin.Banking_app;

import com.benjamin.Banking_app.Security.Role;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createInitialAdmin() {

        if (userRepository.existsByEmail("adminemail@mybank.com")) {
            return;
        }

        Users admin = Users.builder()
                .email("adminemail@mybank.com")
                .password(passwordEncoder.encode("Admin12345"))
                .firstName("System")
                .lastName("Admin")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
    }
}
