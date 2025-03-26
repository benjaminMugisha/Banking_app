package com.benjamin.Banking_app.Loans;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class LoanResponse {
    private String message;
    private Loan loan;

    public LoanResponse(String message) {
        this.message = message;
    }
}
