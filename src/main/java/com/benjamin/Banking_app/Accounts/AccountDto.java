package com.benjamin.Banking_app.Accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AccountDto {
    private long id;
    private String accountUsername;
    private double balance;
}