package com.benjamin.Banking_app;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Security.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtils {

    private final UserRepository userRepository;

    public Account getCurrentUserAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Account account = user.getAccount();
        if (account == null) {
            throw new EntityNotFoundException("Account not found for user: " + email);
        }
        return account;
    }
}
