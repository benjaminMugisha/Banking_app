package com.benjamin.Banking_app.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice //annotation allows you to handle exceptions globally, reducing repetitive code in controllers.
public class BankingHandlerException {

    @ExceptionHandler(value = {EntityNotFoundException.class}) // list of exceptions that will be handled by this method
    public ResponseEntity<Object> handleAccountNotFoundException(
            EntityNotFoundException entityNotFoundException) {
        BankingAppException bankingException = new BankingAppException(
                entityNotFoundException.getMessage(), // message to send to the client
                entityNotFoundException.getCause(),
                HttpStatus.NOT_FOUND
        );
        return new ResponseEntity<>(bankingException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {InsufficientFundsException.class})
    public ResponseEntity<Object> handleInsufficientFundsException(
            InsufficientFundsException insufficientFundsException) {
        BankingAppException bankingException = new BankingAppException(
                insufficientFundsException.getMessage(),
                insufficientFundsException.getCause(),
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(bankingException, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = JwtException.class)
    public ResponseEntity<Object> handleJwtException(JwtException jwtException) {
        BankingAppException bankingException = new BankingAppException(
                jwtException.getMessage(),
                jwtException.getCause(),
                HttpStatus.UNAUTHORIZED
        );

        return new ResponseEntity<>(bankingException, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException exception) {
        BankingAppException bankingException = new BankingAppException(
                exception.getMessage(),
                exception.getCause(),
                HttpStatus.UNAUTHORIZED
        );
        return new ResponseEntity<>(bankingException, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception) {
        BankingAppException bankingException = new BankingAppException(
                exception.getMessage(),
                exception.getCause(),
                HttpStatus.FORBIDDEN
        );
        return new ResponseEntity<>(bankingException, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(value = InvalidJwtSignatureException.class)
    public ResponseEntity<Object> handleInvalidJwtSignatureException(InvalidJwtSignatureException exception) {
        BankingAppException bankingException = new BankingAppException(
                exception.getMessage(),
                exception.getCause(),
                HttpStatus.FORBIDDEN
        );
        return new ResponseEntity<>(bankingException, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<Object> handleJwtTokenExpiredException(TokenExpiredException exception) {
        BankingAppException bankingException = new BankingAppException(
                exception.getMessage(),
                exception.getCause(),
                HttpStatus.UNAUTHORIZED
        );
        return new ResponseEntity<>(bankingException, HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return errorMap;
    }
    @ExceptionHandler(value = {LoanAlreadyPaidException.class}) // list of exceptions that will be handled by this method
    public ResponseEntity<Object> handleLoanAlreadyPaidException(
            LoanAlreadyPaidException loanAlreadyPaidException) {
        LoanAlreadyPaidException loanAlreadyPaidException1 = new LoanAlreadyPaidException(
                loanAlreadyPaidException.getMessage(), // message to send to the client
                HttpStatus.NOT_FOUND
        );
        return new ResponseEntity<>(loanAlreadyPaidException, HttpStatus.NOT_FOUND);
    }
}