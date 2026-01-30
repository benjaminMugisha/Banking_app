package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DirectDebitRepo extends JpaRepository<DirectDebit, Long> {
    List<DirectDebit> findByActiveTrueAndNextPaymentDate(LocalDate date);
    Page<DirectDebit> findByFromAccountAndActiveTrue(Account fromAccount, Pageable pageable);
    DirectDebit findByFromAccountAndToAccount(Account fromAccount, Account toAccount);
//    DirectDebit findAll(Account account, int pageNo, int pageSize );
    List<DirectDebit> findByActiveTrue();

}
