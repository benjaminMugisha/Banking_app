package com.benjamin.Banking_app.Accounts;

import com.benjamin.Banking_app.Security.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "account_username", unique = true)
    private String accountUsername;
    private double balance;

    @OneToOne
    @JoinColumn(name="user_id")
    private Users user;

    public Account(long id, String accountUsername, double balance) {
        this.id = id;
        this.accountUsername = accountUsername;
        this.balance = balance;
    }
}
