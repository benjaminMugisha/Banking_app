package com.benjamin.Banking_app.Exception;

import org.springframework.http.HttpStatus;

public class LoanAlreadyPaidException extends RuntimeException {
    public LoanAlreadyPaidException(String message, HttpStatus notFound){
        super(message);
    }
}
