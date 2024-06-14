package com.benjamin.Banking_app.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountDto {
    private long id;
    private String accountUsername;
    private double balance;
}