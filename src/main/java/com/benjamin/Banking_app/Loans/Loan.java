package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long loanId;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    private BigDecimal remainingBalance;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime startDate;

    private BigDecimal principal;
    private BigDecimal amountToPayEachMonth;
    private LocalDate nextPaymentDate;

    @Builder.Default
    private boolean active = true;
}
