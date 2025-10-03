package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Security.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "account_username", unique = true)
    private String accountUsername;
    private BigDecimal balance;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name="user_id")
    private Users user;

    @Column(unique = true, nullable = false)
    private String iban;

    public Account(long id, String accountUsername, BigDecimal balance) {
        this.id = id;
        this.accountUsername = accountUsername;
        this.balance = balance;
    }
}
