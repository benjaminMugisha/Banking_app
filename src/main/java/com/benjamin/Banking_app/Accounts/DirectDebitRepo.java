package com.benjamin.Banking_app.Accounts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectDebitRepo extends JpaRepository<DirectDebit, Long> {
    List<DirectDebit> findByActiveTrue();
}
