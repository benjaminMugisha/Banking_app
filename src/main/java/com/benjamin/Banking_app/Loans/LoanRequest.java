package com.benjamin.Banking_app.Loans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoanRequest {
    long accountId;
    double income;
    double principal; //the starting loan requested
    int monthsToRepay;
}
