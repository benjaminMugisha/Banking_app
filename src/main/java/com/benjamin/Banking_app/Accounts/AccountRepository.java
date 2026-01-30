package com.benjamin.Banking_app.Accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountUsername(String accountUsername);
    Optional<Account> findByIban(String iban);
    long count();
}
