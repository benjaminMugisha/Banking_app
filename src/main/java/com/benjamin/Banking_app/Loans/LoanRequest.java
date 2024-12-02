package com.benjamin.Banking_app.Loans;

import lombok.Data;

@Data
public class LoanRequest {
    long accountId;
    double income;
    double principal;
    int monthsToRepay;
}
