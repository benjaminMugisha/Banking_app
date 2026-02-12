package com.benjamin.Banking_app.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildResponse(
            String message, HttpStatus status, Map<String, String> validationErrors) {
        ApiError error = new ApiError(
                message,
                status,
                LocalDateTime.now(),
                validationErrors
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handleInsufficientFunds(InsufficientFundsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(LoanAlreadyPaidException.class)
    public ResponseEntity<Object> handleLoanAlreadyPaid(LoanAlreadyPaidException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgument(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        ApiError error = new ApiError(
                "Validation failed: ",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now(),
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
//    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return buildResponse("Access denied.", HttpStatus.FORBIDDEN, null);
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwt(io.jsonwebtoken.ExpiredJwtException ex) {
        return buildResponse("JWT token has expired. Please log in again.", HttpStatus.UNAUTHORIZED, null);
    }

    @ExceptionHandler({io.jsonwebtoken.MalformedJwtException.class, io.jsonwebtoken.SignatureException.class})
    public ResponseEntity<Object> handleInvalidJwt(Exception ex) {
        return buildResponse("Invalid JWT token.", HttpStatus.UNAUTHORIZED, null);
    }
    @ExceptionHandler(EmailAlreadyExists.class)
    public ResponseEntity<Object> handleEmailExists(Exception ex) {
        return buildResponse("Email already in use.", HttpStatus.CONFLICT, null);
    }
    @ExceptionHandler(UserDeactivatedException.class)
    public ResponseEntity<Object> handleUserDeactivated(UserDeactivatedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, null);
    }
}
