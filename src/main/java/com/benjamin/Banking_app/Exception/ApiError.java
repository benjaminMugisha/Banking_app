package com.benjamin.Banking_app.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String message;
    private final HttpStatus httpStatus;
    private LocalDateTime time;

    private Map<String, String> validationErrors;

//    public ApiError(String message, HttpStatus httpStatus, LocalDateTime time){
//        this(message, httpStatus, time, null);
//    }
}
