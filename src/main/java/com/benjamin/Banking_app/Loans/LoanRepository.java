package com.benjamin.Banking_app.Loans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByAccountIdAndRemainingBalanceGreaterThan(long accountId, double balance); //for active loans
    List<Loan> findByAccountId(long accountId);
   List<Loan> findByRemainingBalanceGreaterThan(double value);

}

