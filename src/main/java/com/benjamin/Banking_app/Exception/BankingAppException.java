package com.benjamin.Banking_app.Exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

//exception class where all the exception details of the exception will be and to the client.
@RequiredArgsConstructor
@Getter
public class BankingAppException { //to be printed back to the users
    private final String message; //message we'll send back to the client
    private final Throwable cause;
    private final HttpStatus httpStatus;
}
