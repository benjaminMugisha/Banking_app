package com.benjamin.Banking_app.Accounts;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountUsername(String accountUsername);
}
