package com.benjamin.Banking_app.Accounts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountUsername(String accountUsername);
}
