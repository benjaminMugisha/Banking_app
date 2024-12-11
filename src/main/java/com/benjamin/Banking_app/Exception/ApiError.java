package com.benjamin.Banking_app.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
}

