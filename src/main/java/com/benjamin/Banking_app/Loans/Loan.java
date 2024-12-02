package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long LoanId;

    @ManyToOne //(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false) //foreign key reference
    private Account account;
    private double loanAmount;
    private int interestRate;
    private double amountPaid;
    private double remainingBalance;
    private LocalDateTime startDate;
    private LocalDateTime endDate; //when the loan will be fully repaid
    private double Principal;
    private double amountToPayEachMonth;
}
