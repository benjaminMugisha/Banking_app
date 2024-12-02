package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String type;
    private Double amount;
    private LocalDateTime timestamp;
    private String description;

    // Optional fields for transfers
    @ManyToOne
    @JoinColumn(name = "to_account_id", nullable = true)
    private Account toAccount;

}

