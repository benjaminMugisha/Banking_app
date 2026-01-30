package com.benjamin.Banking_app.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class AdminStats {
    private long totalUsers;
    private long totalAccounts;
    private long totalLoans;
    private long totalDirectDebits;
    private long totalTransactions;
}
