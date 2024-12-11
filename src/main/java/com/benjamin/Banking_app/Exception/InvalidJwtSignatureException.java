package com.benjamin.Banking_app.Exception;

public class InvalidJwtSignatureException extends RuntimeException{
    public InvalidJwtSignatureException(String message) {
        super(message);
    }
}
