package com.benjamin.Banking_app.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String message;
//    private final Throwable cause; //no  because it exposes internal details like stack trace and nested errors. not safe for production
    private final HttpStatus httpStatus;
    private LocalDateTime time;
}
