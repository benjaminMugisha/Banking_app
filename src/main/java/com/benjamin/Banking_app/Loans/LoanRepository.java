package com.benjamin.Banking_app.Loans;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByAccountIdAndRemainingBalanceGreaterThan(long accountId, double balance);
    List<Loan> findByRemainingBalanceGreaterThanAndNextPaymentDate(double balance, LocalDate today);
    Page<Loan> findByAccountIdAndActiveTrue(Long accountId, Pageable pageable);
}
