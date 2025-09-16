package com.benjamin.Banking_app.Security;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;
    private String token;
    @ManyToOne
    private Users user;
    private Instant expiryDate;
}
